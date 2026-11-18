package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.NombreRol;
import cl.duoc.backend.modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByRut(String rut);
    boolean existsByRut(String rut);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    List<Usuario> findByRolNombreAndActivoTrueOrderByNombre(NombreRol rol);
    List<Usuario> findByPasswordHash(String passwordHash);
}
