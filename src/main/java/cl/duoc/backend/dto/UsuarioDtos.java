package cl.duoc.backend.dto;

import cl.duoc.backend.modelo.NombreRol;
import cl.duoc.backend.modelo.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    /** DTO de salida: lo que la API muestra de un usuario. Nunca incluye password_hash. */
    public record UsuarioResponse(Integer id, String rut, String nombre, String email, String telefono,
                                  NombreRol rol, boolean activo, LocalDateTime fechaRegistro) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getRut(), u.getNombre(), u.getEmail(), u.getTelefono(),
                    u.getNombreRol(), u.isActivo(), u.getFechaRegistro());
        }
    }

    /** Alta de usuarios por el administrador (único flujo para crear OPERADOR, REPARTIDOR o ADMINISTRADOR). */
    public record CrearUsuarioRequest(
            @Schema(example = "33.333.333-3") @NotBlank(message = "El RUT es obligatorio") String rut,
            @Schema(example = "Pedro Rojas") @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, max = 100) String nombre,
            @Schema(example = "pedro.rojas@correo.test") @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 120) String email,
            @Schema(example = "+56933333333") @Pattern(regexp = "^\\+?\\d{8,12}$", message = "El teléfono debe tener entre 8 y 12 dígitos") String telefono,
            @Schema(example = "ClaveFicticia123") @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 72) String password,
            @Schema(example = "REPARTIDOR") @NotNull(message = "El rol es obligatorio") NombreRol rol) {
    }

    /** Modificar datos de contacto. No permite cambiar RUT, rol ni contraseña. */
    public record ActualizarUsuarioRequest(
            @Schema(example = "María Soto Pérez") @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, max = 100) String nombre,
            @Schema(example = "maria.soto@correo.test") @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 120) String email,
            @Schema(example = "+56987654321") @Pattern(regexp = "^\\+?\\d{8,12}$", message = "El teléfono debe tener entre 8 y 12 dígitos") String telefono) {
    }

    public record CambiarRolRequest(@Schema(example = "OPERADOR") @NotNull(message = "El rol es obligatorio") NombreRol rol) {
    }

    public record CambiarActivoRequest(@Schema(example = "false") @NotNull(message = "Indica si la cuenta queda activa") Boolean activo) {
    }
}
