package com.gowayki.nest.chat;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.ConflictoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.infrastructure.rest.ChatExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * "Sin sesión" y "sin permiso" no son lo mismo: la app necesita distinguirlos
 * para mandar al login o para mostrar un error de permisos.
 */
class ChatExceptionHandlerTest {

    private final ChatExceptionHandler handler = new ChatExceptionHandler();

    @Test
    void sinIdentidadEs401() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                handler.noAutenticado(new NoAutenticadoException("falta")).getStatusCode());
    }

    @Test
    void sinPermisoEs403() {
        assertEquals(HttpStatus.FORBIDDEN,
                handler.accesoDenegado(new AccesoDenegadoException("no")).getStatusCode());
    }

    @Test
    void estadoIncompatibleEs409() {
        assertEquals(HttpStatus.CONFLICT,
                handler.conflicto(new ConflictoException("pendiente")).getStatusCode());
    }

    @Test
    void peticionMalFormadaEs400() {
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.peticionInvalida(new IllegalArgumentException("mal")).getStatusCode());
    }

    @Test
    void loInesperadoEs500SinFiltrarDetalles() {
        var respuesta = handler.errorInterno(new RuntimeException("password=1234"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertEquals(false, respuesta.getBody().get("exito"));
        assertEquals("Error interno del servidor.", respuesta.getBody().get("mensaje"));
    }
}
