package cl.duoc.backend.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla pago: se registra cuando el pedido pasa a ENTREGADO (máximo uno por pedido). */
@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    @Column(nullable = false)
    private int monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    /** N° de transferencia o voucher (opcional). */
    @Column(length = 50)
    private String comprobante;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    protected Pago() {
    }

    public Pago(Pedido pedido, int monto, String comprobante) {
        this.pedido = pedido;
        this.monto = monto;
        this.comprobante = comprobante;
    }

    public Integer getId() { return id; }
    public int getMonto() { return monto; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public String getComprobante() { return comprobante; }
    public Pedido getPedido() { return pedido; }
}
