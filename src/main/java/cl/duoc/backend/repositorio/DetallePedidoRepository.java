package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    boolean existsByProductoCodigo(String codigo);
}
