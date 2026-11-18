package cl.duoc.backend.modelo;

import jakarta.persistence.*;
import java.time.LocalTime;

/** Tabla zona_despacho: zonas de reparto de Chillán y alrededores. */
@Entity
@Table(name = "zona_despacho")
public class ZonaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private Integer id;

    @Column(nullable = false, unique = true, length = 40)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String cobertura;

    @Column(name = "dias_despacho", nullable = false, length = 60)
    private String diasDespacho;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "tiempo_estimado", length = 30)
    private String tiempoEstimado;

    protected ZonaDespacho() {
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCobertura() { return cobertura; }
    public String getDiasDespacho() { return diasDespacho; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public String getTiempoEstimado() { return tiempoEstimado; }
}
