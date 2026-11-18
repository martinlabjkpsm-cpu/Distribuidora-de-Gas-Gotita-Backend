package cl.duoc.backend.excepcion;

import org.springframework.http.HttpStatus;

/** 401 - credenciales incorrectas o sesión no válida. */
public class NoAutenticadoException extends ApiException {
    public NoAutenticadoException(String mensaje) {
        super(HttpStatus.UNAUTHORIZED, mensaje);
    }
}
