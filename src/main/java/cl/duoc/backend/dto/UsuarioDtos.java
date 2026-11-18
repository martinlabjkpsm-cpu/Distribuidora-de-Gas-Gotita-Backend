package cl.duoc.backend.dto;

import cl.duoc.backend.modelo.Rol;
import cl.duoc.backend.modelo.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    /** Lo que la API muestra de un usuario: sin password_hash. */
    public record UsuarioResponse(Long id, String rut, String nombre, String email, Rol rol, boolean activo) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getRut(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo());
        }
    }

    /** Alta de usuarios internos (operadora, repartidor, administrador): solo el administrador. */
    public record CrearUsuarioRequest(
            @NotBlank(message = "El RUT es obligatorio") String rut,
            @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, max = 100) String nombre,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String email,
            @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 72) String password,
            @NotNull(message = "El rol es obligatorio") Rol rol) {
    }

    public record CambiarRolRequest(@NotNull(message = "El rol es obligatorio") Rol rol) {
    }

    public record CambiarActivoRequest(@NotNull(message = "Indica si la cuenta queda activa") Boolean activo) {
    }
}
