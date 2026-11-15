package com.ferreteria.maestros.common.util;

import java.util.Locale;

/**
 * Utilidades para el RUN/RUT chileno.
 *
 * <p>El front ({@code src/utils/validaciones.js}) valida el RUN con módulo 11 y lo envía
 * sin puntos ni guiones (ej. {@code 19011022K}). El backend normaliza y revalida.</p>
 */
public final class RutUtils {

    private RutUtils() {
    }

    /** Deja solo dígitos y la letra K final, en mayúsculas, sin puntos ni guiones. */
    public static String normalizar(String run) {
        if (run == null) {
            return null;
        }
        return run.trim().toUpperCase(Locale.ROOT).replace(".", "").replace("-", "").replace(" ", "");
    }

    /**
     * Valida un RUN chileno (7–8 dígitos + dígito verificador calculado con módulo 11).
     * @return {@code true} si el RUN es válido.
     */
    public static boolean esValido(String run) {
        String limpio = normalizar(run);
        if (limpio == null || !limpio.matches("^\\d{7,8}[0-9K]$")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dvIngresado = limpio.charAt(limpio.length() - 1);

        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int dvCalculado = 11 - resto;
        char dvEsperado;
        if (dvCalculado == 11) {
            dvEsperado = '0';
        } else if (dvCalculado == 10) {
            dvEsperado = 'K';
        } else {
            dvEsperado = Character.forDigit(dvCalculado, 10);
        }
        return dvIngresado == dvEsperado;
    }
}
