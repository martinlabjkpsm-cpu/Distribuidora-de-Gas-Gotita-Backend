package cl.duoc.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de entrada y salida de /api/v1/auth. */
public final class AuthDtos {

    private AuthDtos() {
    }

    /** Registro público: el rol NO viene del cliente; el servidor asigna CLIENTE. */
    public record RegistroRequest(
            @Schema(example = "12.345.678-5") @NotBlank(message = "El RUT es obligatorio") String rut,
            @Schema(example = "María Soto") @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres") String nombre,
            @Schema(example = "maria.soto@correo.test") @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 120) String email,
            @Schema(example = "+56912345678") @Pattern(regexp = "^\\+?\\d{8,12}$", message = "El teléfono debe tener entre 8 y 12 dígitos (ej. +56912345678)") String telefono,
            @Schema(example = "ClaveFicticia123") @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password) {
    }

    public record LoginRequest(
            @Schema(example = "44.444.444-4", description = "RUT con o sin puntos") @NotBlank(message = "El RUT es obligatorio") String rut,
            @Schema(example = "ClaveFicticia123") @NotBlank(message = "La contraseña es obligatoria") String password) {
    }

    /** Respuesta de login: token de acceso, tipo y vigencia en segundos. Nunca incluye contraseñas ni hashes. */
    @Schema(description = "accessToken es el JWT; tipo siempre Bearer; expiraEnSegundos = vigencia")
    public record TokenResponse(String accessToken, String tipo, long expiraEnSegundos, UsuarioDtos.UsuarioResponse usuario) {
    }
}
