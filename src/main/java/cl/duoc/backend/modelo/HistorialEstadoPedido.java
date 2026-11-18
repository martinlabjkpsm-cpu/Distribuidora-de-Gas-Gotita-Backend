package cl.duoc.backend.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla historial_estado_pedido: una fila por cada cambio de estado (para el seguimiento). */
@Entity
@Table(name = "historial_estado_pedido")
public class HistorialEstadoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer id;

    @Column(nullable = false, length = 12)
    private EstadoPedido estado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    protected HistorialEstadoPedido() {
    }

    public HistorialEstadoPedido(Pedido pedido, EstadoPedido estado) {
        this.pedido = pedido;
        this.estado = estado;
    }

    public Integer getId() { return id; }
    public EstadoPedido getEstado() { return estado; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Pedido getPedido() { return pedido; }
}
