package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.UbicacionRepartidor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionRepartidorRepository extends JpaRepository<UbicacionRepartidor, Integer> {
    Optional<UbicacionRepartidor> findFirstByRepartidorIdOrderByFechaHoraDesc(Integer idRepartidor);
}
