package cl.duoc.backend.servicio;

import cl.duoc.backend.dto.AuthDtos.LoginRequest;
import cl.duoc.backend.dto.AuthDtos.RegistroRequest;
import cl.duoc.backend.dto.AuthDtos.TokenResponse;
import cl.duoc.backend.dto.UsuarioDtos.UsuarioResponse;
import cl.duoc.backend.excepcion.ConflictoException;
import cl.duoc.backend.excepcion.NoAutenticadoException;
import cl.duoc.backend.modelo.Rol;
import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.repositorio.UsuarioRepository;
import cl.duoc.backend.seguridad.LimiteIntentosLogin;
import cl.duoc.backend.seguridad.Rut;
import cl.duoc.backend.seguridad.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    /** Mismo mensaje para RUT inexistente, contraseña incorrecta o cuenta deshabilitada: no revela si la cuenta existe. */
    private static final String CREDENCIALES_INCORRECTAS = "RUT o contraseña incorrectos";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final LimiteIntentosLogin limite;
    /** Hash de relleno: se compara aunque el RUT no exista, para que la respuesta tarde lo mismo. */
    private final String hashDeRelleno;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                       TokenService tokens, LimiteIntentosLogin limite) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.limite = limite;
        this.hashDeRelleno = passwordEncoder.encode("relleno-no-es-una-contrasena");
    }

    /** Registro público: siempre crea un CLIENTE. RUT y correo no pueden repetirse (409). */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        String rut = Rut.validado(req.rut());
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByRut(rut)) {
            throw new ConflictoException("Ya existe una cuenta con ese RUT");
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Ya existe una cuenta con ese correo");
        }
        Usuario nuevo = new Usuario(rut, req.nombre().trim(), email, passwordEncoder.encode(req.password()), Rol.CLIENTE);
        return UsuarioResponse.de(usuarios.save(nuevo));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        String rut = Rut.normalizar(req.rut());
        limite.verificar(rut);

        Usuario u = usuarios.findByRut(rut).orElse(null);
        boolean claveCorrecta = passwordEncoder.matches(req.password(), u != null ? u.getPasswordHash() : hashDeRelleno);

        if (u == null || !claveCorrecta || !u.isActivo()) {
            limite.registrarFallo(rut);
            throw new NoAutenticadoException(CREDENCIALES_INCORRECTAS);
        }
        limite.registrarExito(rut);
        return new TokenResponse(tokens.emitir(u), "Bearer", tokens.vigenciaEnSegundos(), UsuarioResponse.de(u));
    }
}
