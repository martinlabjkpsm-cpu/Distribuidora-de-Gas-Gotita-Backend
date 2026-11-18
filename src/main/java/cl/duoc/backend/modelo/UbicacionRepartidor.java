package cl.duoc.backend.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tabla ubicacion_repartidor: posiciones reportadas por el repartidor (la más reciente es la actual). */
@Entity
@Table(name = "ubicacion_repartidor")
public class UbicacionRepartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer id;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repartidor", nullable = false)
    private Usuario repartidor;

    protected UbicacionRepartidor() {
    }

    public UbicacionRepartidor(Usuario repartidor, BigDecimal latitud, BigDecimal longitud) {
        this.repartidor = repartidor;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Integer getId() { return id; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Usuario getRepartidor() { return repartidor; }
}
