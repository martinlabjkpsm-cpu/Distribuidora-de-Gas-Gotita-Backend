package cl.duoc.backend.excepcion;

import org.springframework.http.HttpStatus;

/** 409 - choca con una regla del negocio o un dato que no se puede duplicar. */
public class ConflictoException extends ApiException {
    public ConflictoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
