package cl.duoc.backend.excepcion;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Convierte las excepciones en respuestas JSON con el estado HTTP del contrato. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    public record RespuestaError(int status, String error, String mensaje, Map<String, String> campos, Instant fecha) {
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje, Map<String, String> campos) {
        return ResponseEntity.status(estado)
                .body(new RespuestaError(estado.value(), estado.getReasonPhrase(), mensaje, campos, Instant.now()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<RespuestaError> negocio(ApiException e) {
        return responder(e.getEstado(), e.getMessage(), null);
    }

    /** 400: falla la validación de @Valid (campos vacíos, formatos, rangos). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", campos);
    }

    /** 400: JSON mal formado o un valor que no existe en un enum (ej. tipoTarifa = "VIP"). */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<RespuestaError> malFormado(Exception e) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo o los parámetros no tienen el formato esperado", null);
    }

    /** 403: @PreAuthorize rechazó al usuario por su rol. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> sinPermiso(AccessDeniedException e) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción", null);
    }

    /** 500: error no previsto. Se registra en el log (sin datos sensibles) y se responde un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> inesperado(Exception e) {
        // Errores propios de Spring MVC (ruta inexistente 404, método no permitido 405...) conservan su estado.
        if (e instanceof org.springframework.web.ErrorResponse er) {
            HttpStatus estado = HttpStatus.valueOf(er.getStatusCode().value());
            return responder(estado, estado.getReasonPhrase(), null);
        }
        log.error("Error no controlado: {}", e.getClass().getSimpleName(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", null);
    }
}
