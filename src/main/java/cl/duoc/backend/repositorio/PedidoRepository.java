package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.EstadoPedido;
import cl.duoc.backend.modelo.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findAllByOrderByFechaDesc();
    List<Pedido> findByEstadoOrderByFechaDesc(EstadoPedido estado);
    List<Pedido> findByClienteRutOrderByFechaDesc(String rut);
    List<Pedido> findByRepartidorRutOrderByFechaDesc(String rut);
}
