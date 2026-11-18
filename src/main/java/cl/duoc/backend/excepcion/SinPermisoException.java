package cl.duoc.backend.excepcion;

import org.springframework.http.HttpStatus;

/** 403 - autenticado pero sin permiso sobre el registro. */
public class SinPermisoException extends ApiException {
    public SinPermisoException(String mensaje) {
        super(HttpStatus.FORBIDDEN, mensaje);
    }
}
