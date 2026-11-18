package cl.duoc.backend.controlador;

import cl.duoc.backend.dto.UsuarioDtos.ActualizarUsuarioRequest;
import cl.duoc.backend.dto.UsuarioDtos.CambiarActivoRequest;
import cl.duoc.backend.dto.UsuarioDtos.CambiarRolRequest;
import cl.duoc.backend.dto.UsuarioDtos.CrearUsuarioRequest;
import cl.duoc.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.duoc.backend.servicio.UsuarioActualService;
import cl.duoc.backend.servicio.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "CRUD de usuarios y roles")
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
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "El rol no es ADMINISTRADOR")
    public List<UsuarioResponse> listar() {
        return usuarios.listar();
    }

    @GetMapping("/repartidores")
    @PreAuthorize("hasAnyRole('OPERADOR','ADMINISTRADOR')")
    @Operation(summary = "Repartidores activos", description = "OPERADOR o ADMINISTRADOR, para asignar pedidos.")
    public List<UsuarioResponse> repartidores() {
        return usuarios.repartidoresActivos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un usuario", description = "El ADMINISTRADOR o la propia persona. Otro id: 403.")
    @ApiResponse(responseCode = "200", description = "Datos del usuario")
    @ApiResponse(responseCode = "403", description = "Es la cuenta de otra persona")
    @ApiResponse(responseCode = "404", description = "No existe")
    public UsuarioResponse obtener(@PathVariable Integer id, Authentication auth) {
        return usuarios.obtener(id, usuarioActual.obtener(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear usuario", description = "ADMINISTRADOR. Único flujo para crear OPERADOR, REPARTIDOR o ADMINISTRADOR.")
    @ApiResponse(responseCode = "201", description = "Creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o RUT incorrecto")
    @ApiResponse(responseCode = "409", description = "RUT o correo ya registrados")
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest req) {
        return usuarios.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar usuario", description = "El ADMINISTRADOR o la propia persona. Cambia nombre, correo y teléfono (no RUT, rol ni contraseña).")
    @ApiResponse(responseCode = "200", description = "Actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "403", description = "Es la cuenta de otra persona")
    @ApiResponse(responseCode = "409", description = "El correo ya lo usa otra cuenta")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ActualizarUsuarioRequest req, Authentication auth) {
        return usuarios.actualizar(id, req, usuarioActual.obtener(auth));
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar rol", description = "ADMINISTRADOR. No puede cambiar su propio rol (409).")
    public UsuarioResponse cambiarRol(@PathVariable Integer id, @Valid @RequestBody CambiarRolRequest req, Authentication auth) {
        return usuarios.cambiarRol(id, req.rol(), usuarioActual.obtener(auth));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Activar o desactivar cuenta", description = "ADMINISTRADOR. Una cuenta desactivada no puede iniciar sesión y sus tokens dejan de servir.")
    public UsuarioResponse cambiarActivo(@PathVariable Integer id, @Valid @RequestBody CambiarActivoRequest req, Authentication auth) {
        return usuarios.cambiarActivo(id, req.activo(), usuarioActual.obtener(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar (desactivar) usuario", description = "ADMINISTRADOR. Desactiva la cuenta en vez de borrarla, para conservar sus pedidos.")
    @ApiResponse(responseCode = "204", description = "Desactivado")
    @ApiResponse(responseCode = "409", description = "Intentó desactivarse a sí mismo")
    public void eliminar(@PathVariable Integer id, Authentication auth) {
        usuarios.cambiarActivo(id, false, usuarioActual.obtener(auth));
    }
}
