package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.DetallePedido;
import cl.duoc.backend.modelo.DetallePedidoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, DetallePedidoId> {
    boolean existsByProductoId(Integer idProducto);
}
