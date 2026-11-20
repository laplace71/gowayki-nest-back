package com.gowayki.nest.infrastructure.websocket;

import com.gowayki.nest.domain.repository.ChatNotificacionPort;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ChatWebSocketNotificacionAdapter implements ChatNotificacionPort {

    private final ChatWebSocketHandler handler;

    public ChatWebSocketNotificacionAdapter(ChatWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void eliminacionPendiente(UUID conversacionId, String titulo, OffsetDateTime ejecutarDespues,
                                     List<UUID> participantes) {
        handler.emitirEventoAUsuarios(participantes, Map.of(
                "tipo", "GRUPO_ELIMINACION_PENDIENTE",
                "conversacionId", conversacionId,
                "titulo", titulo == null ? "" : titulo,
                "ejecutarDespues", ejecutarDespues.toString()
        ));
    }

    @Override
    public void eliminacionCancelada(UUID conversacionId, List<UUID> participantes) {
        handler.emitirEventoAUsuarios(participantes, Map.of(
                "tipo", "GRUPO_ELIMINACION_CANCELADA",
                "conversacionId", conversacionId
        ));
    }

    @Override
    public void grupoEliminado(UUID conversacionId, List<UUID> participantes) {
        handler.emitirEventoAUsuarios(participantes, Map.of(
                "tipo", "CONVERSACION_ELIMINADA",
                "conversacionId", conversacionId
        ));
    }

    @Override
    public void conversacionLimpiada(UUID conversacionId, UUID usuarioId, OffsetDateTime limpiadoEn) {
        handler.emitirEventoAUsuario(usuarioId, Map.of(
                "tipo", "CONVERSACION_LIMPIADA",
                "conversacionId", conversacionId,
                "limpiadoEn", limpiadoEn.toString()
        ));
    }

    @Override
    public void mensajeOculto(UUID conversacionId, UUID mensajeId, UUID usuarioId) {
        handler.emitirEventoAUsuario(usuarioId, Map.of(
                "tipo", "MENSAJE_OCULTO",
                "conversacionId", conversacionId,
                "mensajeId", mensajeId
        ));
    }

    @Override
    public void mensajeEliminado(UUID conversacionId, UUID mensajeId, List<UUID> participantes) {
        handler.emitirEventoAUsuarios(participantes, Map.of(
                "tipo", "MENSAJE_ELIMINADO",
                "conversacionId", conversacionId,
                "mensajeId", mensajeId
        ));
    }
}
