package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.Pago;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    Optional<Pago> findByPedidoId(Integer idPedido);
    boolean existsByPedidoId(Integer idPedido);
}
