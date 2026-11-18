package cl.duoc.backend.controlador;

import cl.duoc.backend.dto.UsuarioDtos.CambiarActivoRequest;
import cl.duoc.backend.dto.UsuarioDtos.CambiarRolRequest;
import cl.duoc.backend.dto.UsuarioDtos.CrearUsuarioRequest;
import cl.duoc.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.duoc.backend.servicio.UsuarioActualService;
import cl.duoc.backend.servicio.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Administración de cuentas y roles")
public class UsuarioController {

    private final UsuarioService usuarios;
    private final UsuarioActualService usuarioActual;

    public UsuarioController(UsuarioService usuarios, UsuarioActualService usuarioActual) {
        this.usuarios = usuarios;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios", description = "ADMINISTRADOR. No incluye hashes de contraseña.")
    public List<UsuarioResponse> listar() {
        return usuarios.listar();
    }

    @GetMapping("/repartidores")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMINISTRADOR')")
    @Operation(summary = "Repartidores activos", description = "OPERADOR o ADMINISTRADOR, para asignar pedidos.")
    public List<UsuarioResponse> repartidores() {
        return usuarios.repartidoresActivos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear cuenta interna", description = "ADMINISTRADOR. Único flujo para crear OPERADOR, REPARTIDOR o ADMINISTRADOR.")
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest req) {
        return usuarios.crear(req);
    }

    @PatchMapping("/{rut}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar rol", description = "ADMINISTRADOR. No puede cambiar su propio rol (409).")
    public UsuarioResponse cambiarRol(@PathVariable String rut, @Valid @RequestBody CambiarRolRequest req, Authentication auth) {
        return usuarios.cambiarRol(rut, req.rol(), usuarioActual.obtener(auth));
    }

    @PatchMapping("/{rut}/activo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Habilitar o deshabilitar cuenta", description = "ADMINISTRADOR. Una cuenta deshabilitada no puede iniciar sesión y sus tokens dejan de servir.")
    public UsuarioResponse cambiarActivo(@PathVariable String rut, @Valid @RequestBody CambiarActivoRequest req, Authentication auth) {
        return usuarios.cambiarActivo(rut, req.activo(), usuarioActual.obtener(auth));
    }
}
