package com.ferreteria.maestros.common.exception;

/**
 * Se lanza cuando una operación no cumple una regla de negocio
 * (p. ej. stock insuficiente, límite de crédito excedido).
 * El {@code GlobalExceptionHandler} la traduce a <b>422 Unprocessable Entity</b>.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
