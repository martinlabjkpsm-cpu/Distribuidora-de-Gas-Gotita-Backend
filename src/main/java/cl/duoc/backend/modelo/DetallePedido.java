package cl.duoc.backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @EmbeddedId
    private DetallePedidoId id;

    @MapsId("idPedido")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @MapsId("idProducto")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    /** Precio congelado al momento de la compra: si después cambia el catálogo, el pedido no cambia. */
    @Column(name = "precio_unitario", nullable = false)
    private int precioUnitario;

    protected DetallePedido() {
    }

    /** El pedido ya debe tener id (se guarda antes de agregar sus detalles). */
    public DetallePedido(Pedido pedido, Producto producto, int cantidad, int precioUnitario) {
        this.id = new DetallePedidoId(pedido.getId(), producto.getId());
        this.pedido = pedido;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    /** No es columna: se calcula. */
    public int getSubtotal() {
        return cantidad * precioUnitario;
    }

    public DetallePedidoId getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public int getPrecioUnitario() { return precioUnitario; }
}
