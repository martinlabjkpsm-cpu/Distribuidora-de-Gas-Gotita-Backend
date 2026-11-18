package cl.duoc.backend.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer id;

    /** Código visible del pedido (PED-1001). Lo genera el servidor a partir del id. */
    @Column(nullable = false, unique = true, length = 12)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(name = "latitud_entrega", precision = 9, scale = 6)
    private BigDecimal latitudEntrega;

    @Column(name = "longitud_entrega", precision = 9, scale = 6)
    private BigDecimal longitudEntrega;

    @Column(length = 300)
    private String indicaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarifa", nullable = false, length = 12)
    private TipoTarifa tipoTarifa;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 15)
    private MetodoPago metodoPago;

    /** Se guarda con EstadoPedidoConverter ('EN CAMINO' con espacio). */
    @Column(nullable = false, length = 12)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    /** Calculado por el servidor como suma de cantidad × precio_unitario. */
    @Column(nullable = false)
    private int total;

    /** Repartidor asignado por la operadora (NULL mientras no se asigna). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repartidor")
    private Usuario repartidor;

    /** Dueño del pedido: se toma del JWT, nunca del cuerpo de la solicitud. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zona", nullable = false)
    private ZonaDespacho zona;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(String codigoTemporal, Usuario cliente, ZonaDespacho zona, String direccion, BigDecimal latitud,
                  BigDecimal longitud, String indicaciones, TipoTarifa tipoTarifa, MetodoPago metodoPago) {
        this.codigo = codigoTemporal;
        this.cliente = cliente;
        this.zona = zona;
        this.direccion = direccion;
        this.latitudEntrega = latitud;
        this.longitudEntrega = longitud;
        this.indicaciones = indicaciones;
        this.tipoTarifa = tipoTarifa;
        this.metodoPago = metodoPago;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        total += detalle.getSubtotal();
    }

    public Integer getId() { return id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDireccion() { return direccion; }
    public BigDecimal getLatitudEntrega() { return latitudEntrega; }
    public BigDecimal getLongitudEntrega() { return longitudEntrega; }
    public String getIndicaciones() { return indicaciones; }
    public TipoTarifa getTipoTarifa() { return tipoTarifa; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public int getTotal() { return total; }
    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }
    public Usuario getCliente() { return cliente; }
    public ZonaDespacho getZona() { return zona; }
    public List<DetallePedido> getDetalles() { return detalles; }
}
