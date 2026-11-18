package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.Producto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("select p from Producto p join fetch p.categoria where p.activo = true order by p.codigo")
    List<Producto> listarActivos();

    @Query("select p from Producto p join fetch p.categoria c where p.activo = true and lower(c.nombre) = lower(:categoria) order by p.codigo")
    List<Producto> listarActivosPorCategoria(@Param("categoria") String categoria);

    @Query("select p from Producto p join fetch p.categoria where p.codigo = :codigo")
    Optional<Producto> buscarPorCodigo(@Param("codigo") String codigo);

    boolean existsByCodigo(String codigo);

    long countByCategoriaId(Integer categoriaId);

    /** Bloquea la fila mientras se descuenta stock, para que dos pedidos simultáneos no vendan lo mismo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.codigo = :codigo")
    Optional<Producto> bloquearPorCodigo(@Param("codigo") String codigo);
}
