package com.gowayki.nest.domain.exception;

/**
 * La petición choca con el estado actual del recurso (por ejemplo, intentar
 * entrar a un grupo que ya está pendiente de eliminación definitiva).
 * El controlador la traduce a HTTP 409.
 */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
