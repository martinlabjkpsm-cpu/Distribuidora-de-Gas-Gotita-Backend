package cl.duoc.backend.repositorio;

import cl.duoc.backend.modelo.Producto;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    @Query("select p from Producto p join fetch p.categoria order by p.codigo")
    List<Producto> listarConCategoria();

    @Query("select p from Producto p join fetch p.categoria c where lower(c.nombre) = lower(:categoria) order by p.codigo")
    List<Producto> listarPorCategoria(@Param("categoria") String categoria);

    long countByCategoriaId(Long categoriaId);

    /** Bloquea la fila mientras se descuenta stock, para que dos pedidos simultáneos no vendan lo mismo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.codigo = :codigo")
    Optional<Producto> bloquearPorCodigo(@Param("codigo") String codigo);
}
