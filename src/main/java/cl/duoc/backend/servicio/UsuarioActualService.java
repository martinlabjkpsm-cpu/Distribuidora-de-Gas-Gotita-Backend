package cl.duoc.backend.servicio;

import cl.duoc.backend.excepcion.NoAutenticadoException;
import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.repositorio.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Obtiene el usuario a partir del JWT ya validado (claim sub = RUT).
 * Nunca se usa un id o RUT enviado por el cliente para saber "quién soy".
 * Si la cuenta fue deshabilitada después de emitir el token, la solicitud se rechaza con 401.
 */
@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarios;

    public UsuarioActualService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario obtener(Authentication auth) {
        if (auth == null) {
            throw new NoAutenticadoException("Debes iniciar sesión");
        }
        Usuario u = usuarios.findByRut(auth.getName())
                .orElseThrow(() -> new NoAutenticadoException("La sesión ya no es válida"));
        if (!u.isActivo()) {
            throw new NoAutenticadoException("La cuenta está deshabilitada");
        }
        return u;
    }
}
