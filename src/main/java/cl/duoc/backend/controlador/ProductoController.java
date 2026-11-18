package cl.duoc.backend.controlador;

import cl.duoc.backend.dto.CatalogoDtos.ProductoRequest;
import cl.duoc.backend.dto.CatalogoDtos.ProductoResponse;
import cl.duoc.backend.servicio.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de cilindros, reguladores, mangueras y accesorios")
public class ProductoController {

    private final CatalogoService catalogo;

    public ProductoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Público. Filtro opcional ?categoria=Reguladores")
    public List<ProductoResponse> listar(@RequestParam(required = false) String categoria) {
        return catalogo.listarProductos(categoria);
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Detalle de un producto", description = "Público. 404 si el código no existe.")
    public ProductoResponse obtener(@PathVariable String codigo) {
        return catalogo.obtenerProducto(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear producto", description = "ADMINISTRADOR. 409 si el código ya existe.")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest req) {
        return catalogo.crearProducto(req);
    }

    @PutMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar producto", description = "ADMINISTRADOR. Precios y stock no negativos.")
    public ProductoResponse actualizar(@PathVariable String codigo, @Valid @RequestBody ProductoRequest req) {
        return catalogo.actualizarProducto(codigo, req);
    }

    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar producto", description = "ADMINISTRADOR. Si el producto ya se vendió, se desactiva (deja de mostrarse) en vez de borrarse.")
    public void eliminar(@PathVariable String codigo) {
        catalogo.eliminarProducto(codigo);
    }
}
