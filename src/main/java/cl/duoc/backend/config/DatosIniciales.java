package cl.duoc.backend.config;

import cl.duoc.backend.modelo.Usuario;
import cl.duoc.backend.repositorio.RolRepository;
import cl.duoc.backend.repositorio.UsuarioRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Al arrancar:
 * 1. Avisa si faltan los roles (hay que ejecutar db/02_datos_ejemplo.sql).
 * 2. Los usuarios de ejemplo del script SQL traen un hash de relleno ('HASH_DE_EJEMPLO_REEMPLAZAR') con el que no se
 *    puede iniciar sesión. Si existe la variable DEMO_PASSWORD, se reemplaza por el hash BCrypt de esa contraseña.
 *    Así ninguna contraseña queda escrita en el repositorio.
 * Las tablas y el catálogo los crean los scripts db/01_crear_tablas.sql y db/02_datos_ejemplo.sql.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    static final String HASH_DE_RELLENO = "HASH_DE_EJEMPLO_REEMPLAZAR";
    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DatosIniciales(RolRepository roles, UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                          @Value("${gotita.demo.password:}") String demoPassword) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roles.count() < 4) {
            log.warn("La tabla rol no tiene los 4 roles: ejecuta db/02_datos_ejemplo.sql antes de registrar usuarios.");
        }
        List<Usuario> pendientes = usuarios.findByPasswordHash(HASH_DE_RELLENO);
        if (pendientes.isEmpty()) {
            return;
        }
        if (demoPassword == null || demoPassword.isBlank()) {
            log.warn("{} usuarios de ejemplo tienen hash de relleno y no pueden iniciar sesión: define DEMO_PASSWORD.", pendientes.size());
            return;
        }
        String hash = passwordEncoder.encode(demoPassword);
        pendientes.forEach(u -> u.setPasswordHash(hash));
        log.info("Contraseña de prueba asignada (BCrypt) a {} usuarios de ejemplo.", pendientes.size());
    }
}
