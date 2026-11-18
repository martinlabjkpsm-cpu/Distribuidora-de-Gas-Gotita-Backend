package cl.duoc.backend.dto;

import cl.duoc.backend.modelo.DetallePedido;
import cl.duoc.backend.modelo.EstadoPedido;
import cl.duoc.backend.modelo.MetodoPago;
import cl.duoc.backend.modelo.Pedido;
import cl.duoc.backend.modelo.TipoTarifa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class PedidoDtos {

    private PedidoDtos() {
    }

    /** El cliente solo dice QUÉ y CUÁNTO; el precio lo pone el servidor. */
    public record ItemRequest(
            @Schema(example = "CL002") @NotBlank(message = "El código del producto es obligatorio") String codigo,
            @Schema(example = "2") @NotNull @Min(value = 1, message = "La cantidad mínima es 1") @Max(value = 50, message = "La cantidad máxima por producto es 50") Integer cantidad) {
    }

    /** No incluye cliente, precios, total ni estado: los define el servidor. */
    public record CrearPedidoRequest(
            @NotEmpty(message = "El pedido debe tener al menos un producto") @Valid List<ItemRequest> items,
            @Schema(example = "RESIDENCIAL") @NotNull(message = "La tarifa es obligatoria") TipoTarifa tipoTarifa,
            @Schema(example = "Av. Libertad 123, Chillán") @NotBlank(message = "La dirección es obligatoria") @Size(min = 5, max = 200, message = "La dirección debe tener al menos 5 caracteres") String direccion,
            @Schema(example = "Zona Centro") @NotBlank(message = "La zona es obligatoria") @Size(max = 60) String zona,
            @Schema(example = "Portón verde") @Size(max = 300) String indicaciones,
            @Schema(example = "EFECTIVO") @NotNull(message = "El método de pago es obligatorio") MetodoPago metodoPago) {
    }

    public record AsignarRepartidorRequest(@Schema(example = "33333333-3") @NotBlank(message = "El RUT del repartidor es obligatorio") String rutRepartidor) {
    }

    public record CambiarEstadoRequest(@Schema(example = "EN_CAMINO") @NotNull(message = "El estado es obligatorio") EstadoPedido estado) {
    }

    public record DetalleResponse(String codigo, String nombre, int cantidad, int precioUnitario, int subtotal) {
        public static DetalleResponse de(DetallePedido d) {
            return new DetalleResponse(d.getProducto().getCodigo(), d.getProducto().getNombre(),
                    d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
        }
    }

    public record PedidoResponse(Long id, String clienteRut, String clienteNombre, String repartidorRut, String repartidorNombre,
                                 String direccion, String zona, String indicaciones, TipoTarifa tipoTarifa, MetodoPago metodoPago,
                                 EstadoPedido estado, int total, Instant fecha, List<DetalleResponse> detalles) {
        public static PedidoResponse de(Pedido p) {
            var rep = p.getRepartidor();
            return new PedidoResponse(p.getId(), p.getCliente().getRut(), p.getCliente().getNombre(),
                    rep == null ? null : rep.getRut(), rep == null ? null : rep.getNombre(),
                    p.getDireccion(), p.getZona(), p.getIndicaciones(), p.getTipoTarifa(), p.getMetodoPago(),
                    p.getEstado(), p.getTotal(), p.getFecha(),
                    p.getDetalles().stream().map(DetalleResponse::de).toList());
        }
    }

    /** "Dinero a rendir" del repartidor: suma de sus pedidos ENTREGADO. */
    public record RendicionResponse(String repartidorRut, long pedidosEntregados, long totalARendir) {
    }
}
