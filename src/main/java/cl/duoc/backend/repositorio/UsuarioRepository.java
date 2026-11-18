package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.Rol;
import cl.duoc.backend.modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByRut(String rut);
    boolean existsByRut(String rut);
    boolean existsByEmailIgnoreCase(String email);
    List<Usuario> findByRolAndActivoTrueOrderByNombre(Rol rol);
}
