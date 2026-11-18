package cl.duoc.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Levanta la aplicación completa: necesita DB_URL, DB_USER, DB_PASSWORD y JWT_SECRET y acceso a RDS.
 * Se deja deshabilitada para que "./mvnw package" no falle sin esas variables.
 * Para ejecutarla: cargar .env.local y quitar @Disabled.
 */
@SpringBootTest
@Disabled("Requiere variables de entorno y conexión a la base")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
