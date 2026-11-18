package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.NombreRol;
import cl.duoc.backend.modelo.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(NombreRol nombre);
}
