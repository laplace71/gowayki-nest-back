package com.gowayki.nest.domain.exception;

/**
 * No hay identidad autenticada en la petición. El controlador la traduce a 401.
 */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
