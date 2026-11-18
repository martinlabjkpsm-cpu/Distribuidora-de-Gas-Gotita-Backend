package cl.duoc.backend.modelo;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dueño del pedido: se toma del JWT, nunca del cuerpo de la solicitud. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    /** Repartidor asignado por la operadora (puede ser nulo mientras está PENDIENTE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 60)
    private String zona;

    @Column(length = 300)
    private String indicaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarifa", nullable = false, length = 20)
    private TipoTarifa tipoTarifa;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    /** Calculado por el servidor como suma de los subtotales. */
    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Usuario cliente, String direccion, String zona, String indicaciones,
                  TipoTarifa tipoTarifa, MetodoPago metodoPago) {
        this.cliente = cliente;
        this.direccion = direccion;
        this.zona = zona;
        this.indicaciones = indicaciones;
        this.tipoTarifa = tipoTarifa;
        this.metodoPago = metodoPago;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
        total += detalle.getSubtotal();
    }

    public Long getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }
    public String getDireccion() { return direccion; }
    public String getZona() { return zona; }
    public String getIndicaciones() { return indicaciones; }
    public TipoTarifa getTipoTarifa() { return tipoTarifa; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public int getTotal() { return total; }
    public Instant getFecha() { return fecha; }
    public List<DetallePedido> getDetalles() { return detalles; }
}
