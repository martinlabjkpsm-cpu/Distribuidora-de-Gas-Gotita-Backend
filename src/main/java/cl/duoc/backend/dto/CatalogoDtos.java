package cl.duoc.backend.dto;

import cl.duoc.backend.modelo.Categoria;
import cl.duoc.backend.modelo.Producto;
import cl.duoc.backend.modelo.ZonaDespacho;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

/** Categorías, productos y zonas de despacho. */
public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record CategoriaRequest(
            @Schema(example = "Reguladores") @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre) {
    }

    public record CategoriaResponse(Integer id, String nombre) {
        public static CategoriaResponse de(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNombre());
        }
    }

    public record ProductoRequest(
            @Schema(example = "CL005") @NotBlank(message = "El código es obligatorio")
            @Pattern(regexp = "^[A-Za-z]{2}\\d{3}$", message = "El código debe tener 2 letras y 3 números (ej. CL001)") String codigo,
            @Schema(example = "Cilindros de Gas") @NotBlank(message = "La categoría es obligatoria") String categoria,
            @Schema(example = "Cilindro GLP 25 kg") @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String nombre,
            @Size(max = 500) String descripcion,
            @Schema(example = "Unidad") @Pattern(regexp = "^(Unidad|Kit)$", message = "La unidad debe ser Unidad o Kit") String unidad,
            @Schema(example = "26000") @NotNull @Positive(message = "El precio debe ser mayor que 0") Integer precioResidencial,
            @Schema(example = "24000") @NotNull @Positive(message = "El precio debe ser mayor que 0") Integer precioComercial,
            @Schema(example = "40") @NotNull @PositiveOrZero(message = "El stock no puede ser negativo") Integer stockActual,
            @Size(max = 255) String imagenSrc,
            @Schema(example = "false") Boolean destacado) {
    }

    /** Mismos nombres de campo que usa el frontend (producto = nombre del producto, imagenSrc = imagen_url). */
    public record ProductoResponse(Integer id, String codigo, String categoria, String producto, String descripcion,
                                   String unidad, int precioResidencial, int precioComercial, int stockActual,
                                   String imagenSrc, boolean destacado) {
        public static ProductoResponse de(Producto p) {
            return new ProductoResponse(p.getId(), p.getCodigo(), p.getCategoria().getNombre(), p.getNombre(),
                    p.getDescripcion(), p.getUnidad(), p.getPrecioResidencial(), p.getPrecioComercial(),
                    p.getStockActual(), p.getImagenUrl(), p.isDestacado());
        }
    }

    public record ZonaResponse(Integer id, String nombre, String cobertura, String diasDespacho,
                               LocalTime horaInicio, LocalTime horaFin, String tiempoEstimado) {
        public static ZonaResponse de(ZonaDespacho z) {
            return new ZonaResponse(z.getId(), z.getNombre(), z.getCobertura(), z.getDiasDespacho(),
                    z.getHoraInicio(), z.getHoraFin(), z.getTiempoEstimado());
        }
    }
}
