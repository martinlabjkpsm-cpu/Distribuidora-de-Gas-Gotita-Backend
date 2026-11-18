package cl.duoc.backend.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cl.duoc.backend.excepcion.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;

/** Prueba unitaria sin Spring ni base de datos: se ejecuta con ./mvnw test -Dtest=RutTest */
class RutTest {

    @Test
    void aceptaRutValidoConYSinPuntos() {
        assertTrue(Rut.esValido("11.111.111-1"));
        assertTrue(Rut.esValido("44444444-4"));
    }

    @Test
    void rechazaDigitoVerificadorIncorrecto() {
        assertFalse(Rut.esValido("11111111-2"));
        assertThrows(SolicitudInvalidaException.class, () -> Rut.validado("12345678-0"));
    }

    @Test
    void normalizaElFormato() {
        assertEquals("12345678-5", Rut.normalizar("12.345.678-5"));
        assertEquals("11111111-K", Rut.normalizar("11111111k"));
    }
}
