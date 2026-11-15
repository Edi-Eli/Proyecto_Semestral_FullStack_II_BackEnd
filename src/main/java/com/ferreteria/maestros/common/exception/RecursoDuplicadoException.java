package com.ferreteria.maestros.common.exception;

/**
 * Se lanza cuando se intenta crear/referenciar un recurso que ya existe
 * (p. ej. correo o RUN duplicados).
 * El {@code GlobalExceptionHandler} la traduce a <b>409 Conflict</b>.
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
