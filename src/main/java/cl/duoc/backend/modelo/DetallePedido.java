package cl.duoc.backend.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_codigo", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    /** Precio congelado al momento de la compra: si después cambia el catálogo, el pedido no cambia. */
    @Column(name = "precio_unitario", nullable = false)
    private int precioUnitario;

    @Column(nullable = false)
    private int subtotal;

    protected DetallePedido() {
    }

    public DetallePedido(Producto producto, int cantidad, int precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }

    public Long getId() { return id; }
    public Pedido getPedido() { return pedido; }
    void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public int getPrecioUnitario() { return precioUnitario; }
    public int getSubtotal() { return subtotal; }
}
