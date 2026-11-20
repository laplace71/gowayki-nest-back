package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.ConflictoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce los errores del chat a códigos HTTP correctos: 401 cuando falta
 * identidad, 403 cuando hay identidad pero no permiso, 400 cuando la petición
 * está mal y 500 para el resto.
 */
@RestControllerAdvice(assignableTypes = ChatController.class)
public class ChatExceptionHandler {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ChatExceptionHandler.class);

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<Map<String, Object>> noAutenticado(NoAutenticadoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("exito", false, "mensaje", e.getMessage()));
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, Object>> accesoDenegado(AccesoDenegadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("exito", false, "mensaje", e.getMessage()));
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> conflicto(ConflictoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("exito", false, "mensaje", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> peticionInvalida(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("exito", false, "mensaje", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> errorInterno(Exception e) {
        // El detalle queda en el log, no en la respuesta: puede contener datos
        // internos (consultas, rutas, credenciales de una conexión).
        log.error("Error no controlado en el chat", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("exito", false, "mensaje", "Error interno del servidor."));
    }
}
