package cl.duoc.backend.servicio;

import cl.duoc.backend.dto.UsuarioDtos.CrearUsuarioRequest;
import cl.duoc.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.duoc.backend.excepcion.ConflictoException;
import cl.duoc.backend.excepcion.NoEncontradoException;
import cl.duoc.backend.modelo.Rol;
import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.repositorio.UsuarioRepository;
import cl.duoc.backend.seguridad.Rut;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administración de cuentas. Solo el ADMINISTRADOR crea cuentas internas y asigna roles. */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll(Sort.by("nombre")).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> repartidoresActivos() {
        return usuarios.findByRolAndActivoTrueOrderByNombre(Rol.REPARTIDOR).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest req) {
        String rut = Rut.validado(req.rut());
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByRut(rut)) {
            throw new ConflictoException("Ya existe una cuenta con ese RUT");
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Ya existe una cuenta con ese correo");
        }
        Usuario u = new Usuario(rut, req.nombre().trim(), email, passwordEncoder.encode(req.password()), req.rol());
        return UsuarioResponse.de(usuarios.save(u));
    }

    /** El administrador no puede cambiarse su propio rol (evita quedarse sin administradores por error). */
    @Transactional
    public UsuarioResponse cambiarRol(String rut, Rol rol, Usuario admin) {
        Usuario u = buscar(rut);
        if (u.getRut().equals(admin.getRut())) {
            throw new ConflictoException("No puedes cambiar tu propio rol");
        }
        u.setRol(rol);
        return UsuarioResponse.de(u);
    }

    /** Deshabilitar impide nuevos logins y hace que sus tokens vigentes sean rechazados (ver UsuarioActualService). */
    @Transactional
    public UsuarioResponse cambiarActivo(String rut, boolean activo, Usuario admin) {
        Usuario u = buscar(rut);
        if (u.getRut().equals(admin.getRut()) && !activo) {
            throw new ConflictoException("No puedes deshabilitar tu propia cuenta");
        }
        u.setActivo(activo);
        return UsuarioResponse.de(u);
    }

    private Usuario buscar(String rut) {
        return usuarios.findByRut(Rut.normalizar(rut))
                .orElseThrow(() -> new NoEncontradoException("No existe el usuario " + rut));
    }
}
