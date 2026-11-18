package cl.duoc.backend.controlador;

import cl.duoc.backend.dto.AuthDtos.LoginRequest;
import cl.duoc.backend.dto.AuthDtos.RegistroRequest;
import cl.duoc.backend.dto.AuthDtos.TokenResponse;
import cl.duoc.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.duoc.backend.servicio.AuthService;
import cl.duoc.backend.servicio.UsuarioActualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login con JWT y datos del usuario autenticado")
public class AuthController {

    private final AuthService auth;
    private final UsuarioActualService usuarioActual;

    public AuthController(AuthService auth, UsuarioActualService usuarioActual) {
        this.auth = auth;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un cliente", description = "Público. El servidor asigna siempre el rol CLIENTE.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o RUT con dígito verificador incorrecto")
    @ApiResponse(responseCode = "409", description = "RUT o correo ya registrados")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return auth.registrar(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Público. Devuelve accessToken (JWT), tipo Bearer y vigencia en segundos.")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas")
    @ApiResponse(responseCode = "401", description = "RUT o contraseña incorrectos (mensaje genérico)")
    @ApiResponse(responseCode = "429", description = "Demasiados intentos fallidos para ese RUT")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Usuario autenticado", description = "Requiere Bearer JWT. La identidad sale del token, no de un id enviado por el cliente.")
    @ApiResponse(responseCode = "200", description = "Datos del usuario")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido")
    public UsuarioResponse me(Authentication authentication) {
        return UsuarioResponse.de(usuarioActual.obtener(authentication));
    }
}
