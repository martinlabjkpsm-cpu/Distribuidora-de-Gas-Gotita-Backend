package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.HistorialEstadoPedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoPedidoRepository extends JpaRepository<HistorialEstadoPedido, Integer> {
    List<HistorialEstadoPedido> findByPedidoIdOrderByFechaHoraAsc(Integer idPedido);
}
