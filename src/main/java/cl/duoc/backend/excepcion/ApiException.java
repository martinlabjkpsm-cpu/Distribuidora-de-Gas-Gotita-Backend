package cl.duoc.backend.excepcion;

import org.springframework.http.HttpStatus;

/** Error del negocio con su estado HTTP. Cada subclase corresponde a un caso del contrato. */
public class ApiException extends RuntimeException {

    private final HttpStatus estado;

    public ApiException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
