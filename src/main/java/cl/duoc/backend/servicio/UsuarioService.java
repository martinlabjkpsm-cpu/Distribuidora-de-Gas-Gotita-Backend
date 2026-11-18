package cl.duoc.backend.servicio;

import cl.duoc.backend.dto.UsuarioDtos.ActualizarUsuarioRequest;
import cl.duoc.backend.dto.UsuarioDtos.CrearUsuarioRequest;
import cl.duoc.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.duoc.backend.excepcion.ConflictoException;
import cl.duoc.backend.excepcion.NoEncontradoException;
import cl.duoc.backend.excepcion.SinPermisoException;
import cl.duoc.backend.modelo.NombreRol;
import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.repositorio.UsuarioRepository;
import cl.duoc.backend.seguridad.Rut;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD de usuarios.
 * - Listar, crear cuentas internas, cambiar rol y desactivar: solo ADMINISTRADOR.
 * - Ver y modificar una cuenta: el ADMINISTRADOR o la propia persona. Cambiar el id en la URL no da acceso a otra cuenta (403).
 * - "Eliminar" desactiva la cuenta: los usuarios están ligados a pedidos y se perdería el historial.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final RolService roles;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, RolService roles, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll(Sort.by("nombre")).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> repartidoresActivos() {
        return usuarios.findByRolNombreAndActivoTrueOrderByNombre(NombreRol.REPARTIDOR).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id, Usuario actual) {
        Usuario u = buscar(id);
        verificarPropioOAdmin(u, actual);
        return UsuarioResponse.de(u);
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
        Usuario u = new Usuario(rut, req.nombre().trim(), email, limpiarTelefono(req.telefono()),
                passwordEncoder.encode(req.password()), roles.obtener(req.rol()));
        return UsuarioResponse.de(usuarios.save(u));
    }

    /** Modifica nombre, correo y teléfono. El correo no puede quedar repetido con otra cuenta (409). */
    @Transactional
    public UsuarioResponse actualizar(Integer id, ActualizarUsuarioRequest req, Usuario actual) {
        Usuario u = buscar(id);
        verificarPropioOAdmin(u, actual);
        String email = req.email().trim().toLowerCase();
        usuarios.findByEmailIgnoreCase(email)
                .filter(otro -> !otro.getId().equals(u.getId()))
                .ifPresent(otro -> { throw new ConflictoException("Ya existe una cuenta con ese correo"); });
        u.setNombre(req.nombre().trim());
        u.setEmail(email);
        u.setTelefono(limpiarTelefono(req.telefono()));
        return UsuarioResponse.de(u);
    }

    /** El administrador no puede cambiarse su propio rol (evita quedarse sin administradores por error). */
    @Transactional
    public UsuarioResponse cambiarRol(Integer id, NombreRol rol, Usuario admin) {
        Usuario u = buscar(id);
        if (u.getId().equals(admin.getId())) {
            throw new ConflictoException("No puedes cambiar tu propio rol");
        }
        u.setRol(roles.obtener(rol));
        return UsuarioResponse.de(u);
    }

    /** Desactivar impide nuevos logins y hace que sus tokens vigentes sean rechazados (ver UsuarioActualService). */
    @Transactional
    public UsuarioResponse cambiarActivo(Integer id, boolean activo, Usuario admin) {
        Usuario u = buscar(id);
        if (u.getId().equals(admin.getId()) && !activo) {
            throw new ConflictoException("No puedes desactivar tu propia cuenta");
        }
        u.setActivo(activo);
        return UsuarioResponse.de(u);
    }

    private Usuario buscar(Integer id) {
        return usuarios.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el usuario " + id));
    }

    private static void verificarPropioOAdmin(Usuario objetivo, Usuario actual) {
        boolean esAdmin = actual.getNombreRol() == NombreRol.ADMINISTRADOR;
        if (!esAdmin && !objetivo.getId().equals(actual.getId())) {
            throw new SinPermisoException("No puedes ver ni modificar la cuenta de otra persona");
        }
    }

    static String limpiarTelefono(String telefono) {
        return (telefono == null || telefono.isBlank()) ? null : telefono.trim();
    }
}
