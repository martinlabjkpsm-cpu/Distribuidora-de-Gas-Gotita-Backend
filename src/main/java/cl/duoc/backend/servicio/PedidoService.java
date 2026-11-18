package cl.duoc.backend.servicio;

import cl.duoc.backend.dto.PedidoDtos.CrearPedidoRequest;
import cl.duoc.backend.dto.PedidoDtos.ItemRequest;
import cl.duoc.backend.dto.PedidoDtos.PedidoResponse;
import cl.duoc.backend.dto.PedidoDtos.RendicionResponse;
import cl.duoc.backend.excepcion.ConflictoException;
import cl.duoc.backend.excepcion.NoEncontradoException;
import cl.duoc.backend.excepcion.SinPermisoException;
import cl.duoc.backend.excepcion.SolicitudInvalidaException;
import cl.duoc.backend.modelo.DetallePedido;
import cl.duoc.backend.modelo.EstadoPedido;
import cl.duoc.backend.modelo.Pedido;
import cl.duoc.backend.modelo.Producto;
import cl.duoc.backend.modelo.Rol;
import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.repositorio.PedidoRepository;
import cl.duoc.backend.repositorio.ProductoRepository;
import cl.duoc.backend.repositorio.UsuarioRepository;
import cl.duoc.backend.seguridad.Rut;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operación principal del negocio: el pedido de gas.
 * - Crear: el servidor toma el cliente del JWT, pone los precios según la tarifa, calcula el total y descuenta stock,
 *   todo en una sola transacción (si un producto falla, no se guarda nada).
 * - Propiedad: un CLIENTE solo ve sus pedidos; un REPARTIDOR solo los que tiene asignados.
 * - Estados: PENDIENTE -> EN_CAMINO -> ENTREGADO; CANCELADO solo desde PENDIENTE (devuelve el stock).
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    @Transactional
    public PedidoResponse crear(CrearPedidoRequest req, Usuario cliente) {
        // Si el mismo código viene dos veces, se suman las cantidades.
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (ItemRequest item : req.items()) {
            cantidades.merge(item.codigo().trim().toUpperCase(), item.cantidad(), Integer::sum);
        }

        Pedido pedido = new Pedido(cliente, req.direccion().trim(), req.zona().trim(),
                req.indicaciones() == null ? null : req.indicaciones().trim(), req.tipoTarifa(), req.metodoPago());

        for (Map.Entry<String, Integer> e : cantidades.entrySet()) {
            Producto p = productos.bloquearPorCodigo(e.getKey())
                    .orElseThrow(() -> new SolicitudInvalidaException("No existe el producto " + e.getKey()));
            int cantidad = e.getValue();
            if (p.getStockActual() < cantidad) {
                throw new ConflictoException("Stock insuficiente para " + p.getNombre()
                        + " (disponible: " + p.getStockActual() + ")");
            }
            p.setStockActual(p.getStockActual() - cantidad);
            pedido.agregarDetalle(new DetallePedido(p, cantidad, p.precioSegun(req.tipoTarifa())));
        }
        return PedidoResponse.de(pedidos.save(pedido));
    }

    /** Cada rol ve una lista distinta; el filtro por estado es opcional. */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Usuario actual, EstadoPedido estado) {
        List<Pedido> lista = switch (actual.getRol()) {
            case CLIENTE -> pedidos.findByClienteRutOrderByFechaDesc(actual.getRut());
            case REPARTIDOR -> pedidos.findByRepartidorRutOrderByFechaDesc(actual.getRut());
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
    public PedidoResponse obtener(Long id, Usuario actual) {
        Pedido p = buscar(id);
        verificarAcceso(p, actual);
        return PedidoResponse.de(p);
    }

    /** OPERADOR o ADMINISTRADOR asigna un repartidor activo a un pedido PENDIENTE. */
    @Transactional
    public PedidoResponse asignarRepartidor(Long id, String rutRepartidor) {
        Pedido p = buscar(id);
        if (p.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ConflictoException("Solo se puede asignar repartidor a pedidos PENDIENTE");
        }
        Usuario rep = usuarios.findByRut(Rut.normalizar(rutRepartidor))
                .filter(u -> u.getRol() == Rol.REPARTIDOR && u.isActivo())
                .orElseThrow(() -> new SolicitudInvalidaException("El RUT no corresponde a un repartidor activo"));
        p.setRepartidor(rep);
        return PedidoResponse.de(p);
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevo, Usuario actual) {
        Pedido p = buscar(id);
        EstadoPedido actualEstado = p.getEstado();

        if (actual.getRol() == Rol.REPARTIDOR) {
            boolean esSuyo = p.getRepartidor() != null && p.getRepartidor().getRut().equals(actual.getRut());
            if (!esSuyo) {
                throw new SinPermisoException("Este pedido no está asignado a ti");
            }
            if (nuevo == EstadoPedido.CANCELADO) {
                throw new SinPermisoException("El repartidor no puede cancelar pedidos");
            }
        }

        boolean valido = switch (nuevo) {
            case EN_CAMINO -> actualEstado == EstadoPedido.PENDIENTE && p.getRepartidor() != null;
            case ENTREGADO -> actualEstado == EstadoPedido.EN_CAMINO;
            case CANCELADO -> actualEstado == EstadoPedido.PENDIENTE;
            case PENDIENTE -> false;
        };
        if (!valido) {
            throw new ConflictoException("No se puede pasar de " + actualEstado + " a " + nuevo
                    + (nuevo == EstadoPedido.EN_CAMINO && p.getRepartidor() == null ? " sin repartidor asignado" : ""));
        }

        if (nuevo == EstadoPedido.CANCELADO) {
            devolverStock(p);
        }
        p.setEstado(nuevo);
        return PedidoResponse.de(p);
    }

    /** Solo ADMINISTRADOR. Si el pedido no se había entregado, el stock vuelve al catálogo. */
    @Transactional
    public void eliminar(Long id) {
        Pedido p = buscar(id);
        if (p.getEstado() == EstadoPedido.PENDIENTE || p.getEstado() == EstadoPedido.EN_CAMINO) {
            devolverStock(p);
        }
        pedidos.delete(p);
    }

    /** "Dinero a rendir": total de los pedidos ENTREGADO del repartidor que consulta. */
    @Transactional(readOnly = true)
    public RendicionResponse rendicion(Usuario repartidor) {
        List<Pedido> entregados = pedidos.findByRepartidorRutOrderByFechaDesc(repartidor.getRut()).stream()
                .filter(p -> p.getEstado() == EstadoPedido.ENTREGADO)
                .toList();
        long total = entregados.stream().mapToLong(Pedido::getTotal).sum();
        return new RendicionResponse(repartidor.getRut(), entregados.size(), total);
    }

    private Pedido buscar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el pedido " + id));
    }

    /** Regla de propiedad: cambiar el id en la URL no permite ver pedidos ajenos (403). */
    private void verificarAcceso(Pedido p, Usuario actual) {
        boolean permitido = switch (actual.getRol()) {
            case ADMINISTRADOR, OPERADOR -> true;
            case CLIENTE -> p.getCliente().getRut().equals(actual.getRut());
            case REPARTIDOR -> p.getRepartidor() != null && p.getRepartidor().getRut().equals(actual.getRut());
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
