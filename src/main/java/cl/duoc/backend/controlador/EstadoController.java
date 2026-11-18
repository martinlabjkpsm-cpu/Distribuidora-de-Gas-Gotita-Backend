package cl.duoc.backend.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Estado", description = "Comprobar que la API y la base de datos responden")
public class EstadoController {

    private final JdbcTemplate jdbc;

    public EstadoController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/estado")
    @Operation(summary = "Estado de la API", description = "Público. Devuelve el nombre de la base conectada.")
    public Map<String, String> estado() {
        String base = jdbc.queryForObject("select current_database()", String.class);
        return Map.of("api", "ok", "baseDatos", base);
    }
}
