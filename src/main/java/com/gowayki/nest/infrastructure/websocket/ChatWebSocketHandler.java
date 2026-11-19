package com.gowayki.nest.infrastructure.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket del chat.
 *
 * Los eventos personales (ocultar una conversación) solo se envían al usuario
 * que los provocó; para eso el cliente se identifica al conectar con
 * `/ws/chat?usuarioId=<uuid>`. Los mensajes nuevos se siguen difundiendo a todos,
 * como antes.
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    /** sesión -> usuario que la abrió (si lo indicó al conectar). */
    private final Map<String, UUID> usuariosPorSesion = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        usuarioDeLaSesion(session).ifPresent(usuarioId ->
                usuariosPorSesion.put(session.getId(), usuarioId));
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        usuariosPorSesion.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
        usuariosPorSesion.remove(session.getId());
    }

    public void emitirMensaje(Object mensajeDto) {
        enviarATodas(mensajeDto);
    }

    /** Difunde un evento que no es un mensaje. */
    public void emitirEvento(Object evento) {
        enviarATodas(evento);
    }

    /** Envía un evento únicamente a las sesiones de ese usuario. */
    public void emitirEventoAUsuario(UUID usuarioId, Object evento) {
        if (usuarioId == null) return;
        emitirEventoAUsuarios(List.of(usuarioId), evento);
    }

    /** Envía un evento a las sesiones de todos esos usuarios. */
    public void emitirEventoAUsuarios(java.util.Collection<UUID> usuarios, Object evento) {
        if (usuarios == null || usuarios.isEmpty()) return;
        String json = serializar(evento);
        if (json == null) return;

        for (Map.Entry<String, UUID> entrada : usuariosPorSesion.entrySet()) {
            if (!usuarios.contains(entrada.getValue())) continue;
            sessions.stream()
                    .filter(sesion -> sesion.getId().equals(entrada.getKey()))
                    .findFirst()
                    .ifPresent(sesion -> enviar(sesion, json));
        }
    }

    private void enviarATodas(Object payload) {
        String json = serializar(payload);
        if (json == null) return;
        for (WebSocketSession session : sessions) {
            enviar(session, json);
        }
    }

    private void enviar(WebSocketSession session, String json) {
        if (!session.isOpen()) return;
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException ignored) {
        }
    }

    private String serializar(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Usuario autenticado en el handshake (lo pone `JwtHandshakeInterceptor`).
     * Nunca se toma de la URL: cualquiera podría escribir un id ajeno.
     */
    private Optional<UUID> usuarioDeLaSesion(WebSocketSession session) {
        try {
            Object valor = session.getAttributes().get(JwtHandshakeInterceptor.ATRIBUTO_USUARIO);
            return valor instanceof UUID usuarioId ? Optional.of(usuarioId) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
