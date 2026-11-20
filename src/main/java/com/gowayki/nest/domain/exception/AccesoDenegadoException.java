package com.gowayki.nest.domain.exception;

/**
 * El solicitante está autenticado pero no tiene permiso sobre el recurso.
 * El controlador la traduce a HTTP 403.
 */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
