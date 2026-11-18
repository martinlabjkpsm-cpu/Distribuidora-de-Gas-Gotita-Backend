package cl.duoc.backend.excepcion;

import org.springframework.http.HttpStatus;

/** 429 - login bloqueado temporalmente. */
public class DemasiadosIntentosException extends ApiException {
    public DemasiadosIntentosException(String mensaje) {
        super(HttpStatus.TOO_MANY_REQUESTS, mensaje);
    }
}
