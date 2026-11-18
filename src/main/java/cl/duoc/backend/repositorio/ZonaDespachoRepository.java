package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.ZonaDespacho;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaDespachoRepository extends JpaRepository<ZonaDespacho, Integer> {
    Optional<ZonaDespacho> findByNombreIgnoreCase(String nombre);
}
