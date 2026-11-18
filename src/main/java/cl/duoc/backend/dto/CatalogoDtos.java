package cl.duoc.backend.dto;

import cl.duoc.backend.modelo.Categoria;
import cl.duoc.backend.modelo.Producto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Categorías y productos del catálogo. */
public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record CategoriaRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre) {
    }

    public record CategoriaResponse(Long id, String nombre) {
        public static CategoriaResponse de(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre());
        }
    }

    public record ProductoRequest(
            @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "^[A-Za-z]{2}\\d{3}$", message = "El código debe tener 2 letras y 3 números (ej. CL001)") String codigo,
            @NotBlank(message = "La categoría es obligatoria") String categoria,
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
            @Size(max = 500) String descripcion,
            @Size(max = 20) String unidad,
            @NotNull @PositiveOrZero(message = "El precio no puede ser negativo") Integer precioResidencial,
            @NotNull @PositiveOrZero(message = "El precio no puede ser negativo") Integer precioComercial,
            @NotNull @PositiveOrZero(message = "El stock no puede ser negativo") Integer stockActual,
            @Size(max = 200) String imagenSrc) {
    }

    /** Mismos nombres de campo que usa el frontend (producto = nombre del producto). */
    public record ProductoResponse(String codigo, String categoria, String producto, String descripcion, String unidad,
                                   int precioResidencial, int precioComercial, int stockActual, String imagenSrc) {
        public static ProductoResponse de(Producto p) {
            return new ProductoResponse(p.getCodigo(), p.getCategoria().getNombre(), p.getNombre(), p.getDescripcion(),
                    p.getUnidad(), p.getPrecioResidencial(), p.getPrecioComercial(), p.getStockActual(), p.getImagenSrc());
        }
    }
}
