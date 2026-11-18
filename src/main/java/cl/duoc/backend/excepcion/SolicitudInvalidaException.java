package cl.duoc.backend.excepcion;

import org.springframework.http.HttpStatus;

/** 400 - dato que incumple el contrato. */
public class SolicitudInvalidaException extends ApiException {
    public SolicitudInvalidaException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, mensaje);
    }
}
