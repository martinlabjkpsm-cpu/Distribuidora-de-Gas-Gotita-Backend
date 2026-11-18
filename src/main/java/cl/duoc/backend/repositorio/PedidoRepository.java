package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.EstadoPedido;
import cl.duoc.backend.modelo.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    Optional<Pedido> findByCodigo(String codigo);
    List<Pedido> findAllByOrderByFechaDesc();
    List<Pedido> findByEstadoOrderByFechaDesc(EstadoPedido estado);
    List<Pedido> findByClienteIdOrderByFechaDesc(Integer idCliente);
    List<Pedido> findByRepartidorIdOrderByFechaDesc(Integer idRepartidor);
    boolean existsByClienteIdOrRepartidorId(Integer idCliente, Integer idRepartidor);
}
