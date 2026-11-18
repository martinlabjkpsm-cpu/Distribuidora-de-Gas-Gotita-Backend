package cl.duoc.backend.modelo;

import jakarta.persistence.*;

/** Tabla rol: los 4 roles del caso. Se cargan con db/02_datos_ejemplo.sql. */
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private NombreRol nombre;

    @Column(length = 150)
    private String descripcion;

    protected Rol() {
    }

    public Integer getId() { return id; }
    public NombreRol getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
}
