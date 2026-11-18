package cl.duoc.backend.seguridad;

import cl.duoc.backend.excepcion.SolicitudInvalidaException;

/** Normaliza y valida RUT chilenos (dígito verificador módulo 11), igual que validaciones.js del frontend. */
public final class Rut {

    private Rut() {
    }

    /** "12.345.678-5" o "123456785" -> "12345678-5". No valida. */
    public static String normalizar(String rut) {
        if (rut == null) return "";
        String limpio = rut.replaceAll("[^0-9kK]", "").toUpperCase();
        if (limpio.length() < 2) return limpio;
        return limpio.substring(0, limpio.length() - 1) + "-" + limpio.charAt(limpio.length() - 1);
    }

    public static boolean esValido(String rut) {
        String limpio = rut == null ? "" : rut.replaceAll("[^0-9kK]", "").toUpperCase();
        if (limpio.length() < 8 || limpio.length() > 9) return false;
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        if (!cuerpo.matches("\\d+")) return false;
        int suma = 0;
        int multiplo = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplo;
            multiplo = multiplo == 7 ? 2 : multiplo + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : Character.forDigit(resto, 10);
        return dv == esperado;
    }

    /** Normaliza y lanza 400 si el RUT no es válido. */
    public static String validado(String rut) {
        if (!esValido(rut)) {
            throw new SolicitudInvalidaException("El RUT no es válido");
        }
        return normalizar(rut);
    }
}
