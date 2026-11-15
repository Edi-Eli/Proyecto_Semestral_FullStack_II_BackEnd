package com.ferreteria.maestros.common.validation;

/**
 * Patrones y mensajes de validación compartidos por los DTOs (Bean Validation), para no
 * duplicar expresiones regulares en cada request.
 */
public final class Patrones {

    private Patrones() {
    }

    /** Correos permitidos (mismos dominios que valida el front). */
    public static final String CORREO_DOMINIO_PERMITIDO =
            "(?i)^[^\\s@]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$";

    public static final String MENSAJE_CORREO_DOMINIO_PERMITIDO =
            "El correo debe pertenecer a @duoc.cl, @profesor.duoc.cl o @gmail.com.";

    /** Preferencia de entrega válida. */
    public static final String PREFERENCIA_ENTREGA = "RETIRO|DESPACHO";

    public static final String MENSAJE_PREFERENCIA_ENTREGA =
            "La preferencia de entrega debe ser RETIRO o DESPACHO.";

    /** Contraseña del sistema (regla del curso y del front). */
    public static final int PASSWORD_MIN = 4;
    public static final int PASSWORD_MAX = 10;

    public static final String MENSAJE_PASSWORD =
            "La contraseña debe tener entre 4 y 10 caracteres.";
}
