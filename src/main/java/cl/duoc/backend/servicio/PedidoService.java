package cl.duoc.backend.servicio;

import cl.duoc.backend.dto.PedidoDtos.CrearPedidoRequest;
import cl.duoc.backend.dto.PedidoDtos.HistorialResponse;
import cl.duoc.backend.dto.PedidoDtos.ItemRequest;
import cl.duoc.backend.dto.PedidoDtos.PedidoResponse;
import cl.duoc.backend.dto.PedidoDtos.RendicionResponse;
import cl.duoc.backend.excepcion.ConflictoException;
import cl.duoc.backend.excepcion.NoEncontradoException;
import cl.duoc.backend.excepcion.SinPermisoException;
import cl.duoc.backend.excepcion.SolicitudInvalidaException;
import cl.duoc.backend.modelo.DetallePedido;
import cl.duoc.backend.modelo.EstadoPedido;
import cl.duoc.backend.modelo.HistorialEstadoPedido;
import cl.duoc.backend.modelo.NombreRol;
import cl.duoc.backend.modelo.Pago;
import cl.duoc.backend.modelo.Pedido;
import cl.duoc.backend.modelo.Producto;
import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.modelo.ZonaDespacho;
import cl.duoc.backend.repositorio.HistorialEstadoPedidoRepository;
import cl.duoc.backend.repositorio.PagoRepository;
import cl.duoc.backend.repositorio.PedidoRepository;
import cl.duoc.backend.repositorio.ProductoRepository;
import cl.duoc.backend.repositorio.UsuarioRepository;
import cl.duoc.backend.repositorio.ZonaDespachoRepository;
import cl.duoc.backend.seguridad.Rut;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operación principal del negocio: el pedido de gas.
 * - Crear: el servidor toma el cliente del JWT, pone los precios según la tarifa, calcula el total, descuenta stock,
 *   genera el código PED-xxxx y registra el historial, todo en una sola transacción.
 * - Propiedad: un CLIENTE solo ve sus pedidos; un REPARTIDOR solo los que tiene asignados.
 * - Regla con conflicto (409): un pedido pasa a EN CAMINO solo si tiene repartidor asignado.
 * - Estados: PENDIENTE -> EN_CAMINO -> ENTREGADO; CANCELADO solo desde PENDIENTE (devuelve el stock).
 * - Al pasar a ENTREGADO se registra el pago (tabla pago).
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final ZonaDespachoRepository zonas;
    private final HistorialEstadoPedidoRepository historial;
    private final PagoRepository pagos;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos, UsuarioRepository usuarios,
                         ZonaDespachoRepository zonas, HistorialEstadoPedidoRepository historial, PagoRepository pagos) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.usuarios = usuarios;
        this.zonas = zonas;
        this.historial = historial;
        this.pagos = pagos;
    }

    @Transactional
    public PedidoResponse crear(CrearPedidoRequest req, Usuario cliente) {
        ZonaDespacho zona = zonas.findByNombreIgnoreCase(req.zona().trim())
                .orElseThrow(() -> new SolicitudInvalidaException("La zona " + req.zona() + " no existe"));

        // Si el mismo código viene dos veces, se suman las cantidades (la PK del detalle no permite repetirlo).
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (ItemRequest item : req.items()) {
            cantidades.merge(item.codigo().trim().toUpperCase(), item.cantidad(), Integer::sum);
        }

        // 1. Validar y bloquear cada producto antes de guardar nada.
        Map<Producto, Integer> lineas = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : cantidades.entrySet()) {
            Producto p = productos.bloquearPorCodigo(e.getKey())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new SolicitudInvalidaException("No existe el producto " + e.getKey()));
            if (p.getStockActual() < e.getValue()) {
                throw new ConflictoException("Stock insuficiente para " + p.getNombre()
                        + " (disponible: " + p.getStockActual() + ")");
            }
            lineas.put(p, e.getValue());
        }

        // 2. Guardar la cabecera con un código temporal único para obtener el id; luego el código definitivo PED-xxxx.
        String temporal = "TMP-" + UUID.randomUUID().toString().substring(0, 8);
        Pedido pedido = new Pedido(temporal, cliente, zona, req.direccion().trim(), req.latitud(), req.longitud(),
                req.indicaciones() == null ? null : req.indicaciones().trim(), req.tipoTarifa(), req.metodoPago());
        pedido = pedidos.saveAndFlush(pedido);
        pedido.setCodigo("PED-" + (1000 + pedido.getId()));

        // 3. Detalles con el precio según la tarifa (lo decide el servidor) y descuento de stock.
        for (Map.Entry<Producto, Integer> l : lineas.entrySet()) {
            Producto p = l.getKey();
            int cantidad = l.getValue();
            p.setStockActual(p.getStockActual() - cantidad);
            pedido.agregarDetalle(new DetallePedido(pedido, p, cantidad, p.precioSegun(req.tipoTarifa())));
        }
        historial.save(new HistorialEstadoPedido(pedido, EstadoPedido.PENDIENTE));
        return PedidoResponse.de(pedidos.save(pedido));
    }

    /** Cada rol ve una lista distinta; el filtro por estado es opcional. */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Usuario actual, EstadoPedido estado) {
        List<Pedido> lista = switch (actual.getNombreRol()) {
            case CLIENTE -> pedidos.findByClienteIdOrderByFechaDesc(actual.getId());
            case REPARTIDOR -> pedidos.findByRepartidorIdOrderByFechaDesc(actual.getId());
            case OPERADOR, ADMINISTRADOR -> estado == null
                    ? pedidos.findAllByOrderByFechaDesc()
                    : pedidos.findByEstadoOrderByFechaDesc(estado);
        };
        return lista.stream()
                .filter(p -> estado == null || p.getEstado() == estado)
                .map(PedidoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Integer id, Usuario actual) {
        Pedido p = buscar(id);
        verificarAcceso(p, actual);
        return PedidoResponse.de(p);
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> historial(Integer id, Usuario actual) {
        Pedido p = buscar(id);
        verificarAcceso(p, actual);
        return historial.findByPedidoIdOrderByFechaHoraAsc(p.getId()).stream().map(HistorialResponse::de).toList();
    }

    /** OPERADOR o ADMINISTRADOR asigna un repartidor activo a un pedido PENDIENTE. */
    @Transactional
    public PedidoResponse asignarRepartidor(Integer id, String rutRepartidor) {
        Pedido p = buscar(id);
        if (p.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ConflictoException("Solo se puede asignar repartidor a pedidos PENDIENTE");
        }
        Usuario rep = usuarios.findByRut(Rut.normalizar(rutRepartidor))
                .filter(u -> u.getNombreRol() == NombreRol.REPARTIDOR && u.isActivo())
                .orElseThrow(() -> new SolicitudInvalidaException("El RUT no corresponde a un repartidor activo"));
        p.setRepartidor(rep);
        return PedidoResponse.de(p);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Integer id, EstadoPedido nuevo, String comprobante, Usuario actual) {
        Pedido p = buscar(id);
        EstadoPedido estadoActual = p.getEstado();

        if (actual.getNombreRol() == NombreRol.REPARTIDOR) {
            boolean esSuyo = p.getRepartidor() != null && p.getRepartidor().getId().equals(actual.getId());
            if (!esSuyo) {
                throw new SinPermisoException("Este pedido no está asignado a ti");
            }
            if (nuevo == EstadoPedido.CANCELADO) {
                throw new SinPermisoException("El repartidor no puede cancelar pedidos");
            }
        }

        // Regla de negocio con conflicto: sin repartidor asignado no se puede salir a reparto.
        if (nuevo == EstadoPedido.EN_CAMINO && p.getRepartidor() == null) {
            throw new ConflictoException("El pedido no puede pasar a EN CAMINO sin repartidor asignado");
        }
        boolean valido = switch (nuevo) {
            case EN_CAMINO -> estadoActual == EstadoPedido.PENDIENTE;
            case ENTREGADO -> estadoActual == EstadoPedido.EN_CAMINO;
            case CANCELADO -> estadoActual == EstadoPedido.PENDIENTE;
            case PENDIENTE -> false;
        };
        if (!valido) {
            throw new ConflictoException("No se puede pasar de " + estadoActual + " a " + nuevo);
        }

        if (nuevo == EstadoPedido.CANCELADO) {
            devolverStock(p);
        }
        p.setEstado(nuevo);
        historial.save(new HistorialEstadoPedido(p, nuevo));
        if (nuevo == EstadoPedido.ENTREGADO && !pagos.existsByPedidoId(p.getId())) {
            pagos.save(new Pago(p, p.getTotal(), comprobante == null || comprobante.isBlank() ? null : comprobante.trim()));
        }
        return PedidoResponse.de(p);
    }

    /** Solo ADMINISTRADOR. Un pedido con pago registrado no se elimina; si no estaba entregado, el stock vuelve. */
    @Transactional
    public void eliminar(Integer id) {
        Pedido p = buscar(id);
        if (pagos.existsByPedidoId(p.getId())) {
            throw new ConflictoException("No se puede eliminar un pedido entregado y pagado");
        }
        if (p.getEstado() == EstadoPedido.PENDIENTE || p.getEstado() == EstadoPedido.EN_CAMINO) {
            devolverStock(p);
        }
        pedidos.delete(p);
    }

    /** "Dinero a rendir": total de los pedidos ENTREGADO del repartidor que consulta. */
    @Transactional(readOnly = true)
    public RendicionResponse rendicion(Usuario repartidor) {
        List<Pedido> entregados = pedidos.findByRepartidorIdOrderByFechaDesc(repartidor.getId()).stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .toList();
        long total = entregados.stream().mapToLong(Pedido::getTotal).sum();
        return new RendicionResponse(repartidor.getRut(), entregados.size(), total);
    }

    private Pedido buscar(Integer id) {
        return pedidos.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el pedido " + id));
    }

    /** Regla de propiedad: cambiar el id en la URL no permite ver pedidos ajenos (403). */
    private void verificarAcceso(Pedido p, Usuario actual) {
        boolean permitido = switch (actual.getNombreRol()) {
            case ADMINISTRADOR, OPERADOR -> true;
            case CLIENTE -> p.getCliente().getId().equals(actual.getId());
            case REPARTIDOR -> p.getRepartidor() != null && p.getRepartidor().getId().equals(actual.getId());
        };
        if (!permitido) {
            throw new SinPermisoException("No tienes permiso para ver este pedido");
        }
    }

    private void devolverStock(Pedido p) {
        for (DetallePedido d : p.getDetalles()) {
            Producto prod = d.getProducto();
            prod.setStockActual(prod.getStockActual() + d.getCantidad());
        }
    }
}
