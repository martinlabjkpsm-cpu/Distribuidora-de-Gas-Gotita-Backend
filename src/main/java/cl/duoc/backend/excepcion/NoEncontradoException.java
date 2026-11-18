package cl.duoc.backend.excepcion;

import org.springframework.http.HttpStatus;

/** 404 - el recurso no existe. */
public class NoEncontradoException extends ApiException {
    public NoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }
}
