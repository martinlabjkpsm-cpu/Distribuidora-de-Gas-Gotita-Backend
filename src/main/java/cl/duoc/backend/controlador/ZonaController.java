package cl.duoc.backend.controlador;

import cl.duoc.backend.dto.CatalogoDtos.ZonaResponse;
import cl.duoc.backend.servicio.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/zonas")
@Tag(name = "Zonas de despacho")
public class ZonaController {

    private final CatalogoService catalogo;

    public ZonaController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping
    @Operation(summary = "Listar zonas de despacho", description = "Público. El formulario de pedido envía el nombre de la zona.")
    public List<ZonaResponse> listar() {
        return catalogo.listarZonas();
    }
}
