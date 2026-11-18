package cl.duoc.backend.seguridad;

import cl.duoc.backend.excepcion.DemasiadosIntentosException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Límite simple de intentos de login por RUT: 5 fallos seguidos bloquean ese RUT 15 minutos (429).
 * Vive en memoria: se reinicia al reiniciar el backend y no se comparte entre varias instancias (pendiente documentado).
 */
@Component
public class LimiteIntentosLogin {

    private static final int MAX_FALLOS = 5;
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    private record Registro(int fallos, Instant bloqueadoHasta) {
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    public void verificar(String rut) {
        Registro r = registros.get(rut);
        if (r != null && r.bloqueadoHasta() != null && Instant.now().isBefore(r.bloqueadoHasta())) {
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Intenta de nuevo en unos minutos");
        }
    }

    public void registrarFallo(String rut) {
        registros.compute(rut, (k, r) -> {
            int fallos = (r == null || r.bloqueadoHasta() != null) ? 1 : r.fallos() + 1;
            return fallos >= MAX_FALLOS ? new Registro(0, Instant.now().plus(BLOQUEO)) : new Registro(fallos, null);
        });
    }

    public void registrarExito(String rut) {
        registros.remove(rut);
    }
}
