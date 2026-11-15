package com.ferreteria.maestros.common.exception;

/**
 * Se lanza cuando se busca una entidad que no existe.
 * El {@code GlobalExceptionHandler} la traduce a <b>404 Not Found</b>.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
