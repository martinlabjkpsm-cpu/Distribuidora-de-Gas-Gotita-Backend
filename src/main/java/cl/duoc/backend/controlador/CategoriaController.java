package cl.duoc.backend.controlador;

import cl.duoc.backend.dto.CatalogoDtos.CategoriaRequest;
import cl.duoc.backend.dto.CatalogoDtos.CategoriaResponse;
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
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías")
public class CategoriaController {

    private final CatalogoService catalogo;

    public CategoriaController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Público.")
    public List<CategoriaResponse> listar() {
        return catalogo.listarCategorias();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear categoría", description = "ADMINISTRADOR. 409 si el nombre ya existe.")
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest req) {
        return catalogo.crearCategoria(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Renombrar categoría", description = "ADMINISTRADOR. 404 si no existe, 409 si el nombre ya está en uso.")
    public CategoriaResponse renombrar(@PathVariable Integer id, @Valid @RequestBody CategoriaRequest req) {
        return catalogo.renombrarCategoria(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar categoría", description = "ADMINISTRADOR. 409 si todavía tiene productos.")
    public void eliminar(@PathVariable Integer id) {
        catalogo.eliminarCategoria(id);
    }
}
