package cl.duoc.backend.servicio;

import cl.duoc.backend.modelo.NombreRol;
import cl.duoc.backend.modelo.Rol;
import cl.duoc.backend.repositorio.RolRepository;
import org.springframework.stereotype.Service;

/** Busca los roles de la tabla rol (cargados con db/02_datos_ejemplo.sql). */
@Service
public class RolService {

    private final RolRepository roles;

    public RolService(RolRepository roles) {
        this.roles = roles;
    }

    public Rol obtener(NombreRol nombre) {
        return roles.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombre + " en la tabla rol: ejecuta db/02_datos_ejemplo.sql"));
    }
}
