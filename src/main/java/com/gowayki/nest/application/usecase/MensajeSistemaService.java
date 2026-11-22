package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.MensajeResponseDto;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ChatNotificacionPort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Avisos de sistema dentro de una conversación ("salió del grupo", "se unió").
 * Se guardan como mensajes de tipo SISTEMA, se publican por WebSocket y,
 * como llevan fecha de ahora, respetan los filtros de "limpiar chat".
 */
@Service
public class MensajeSistemaService {

    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final ChatNotificacionPort chatNotificacionPort;

    public MensajeSistemaService(MensajeRepositoryPort mensajeRepositoryPort,
                                 UsuarioRepositoryPort usuarioRepositoryPort,
                                 ConversacionRepositoryPort conversacionRepositoryPort,
                                 ChatNotificacionPort chatNotificacionPort) {
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.chatNotificacionPort = chatNotificacionPort;
    }

    public void avisar(UUID conversacionId, UUID usuarioId, String accion) {
        if (conversacionId == null || usuarioId == null) return;
        String nombre = usuarioRepositoryPort.findById(usuarioId)
                .map(u -> u.getNombre())
                .orElse("Un conductor");
        MensajeModel guardado = mensajeRepositoryPort.save(MensajeModel.builder()
                .conversacionId(conversacionId)
                .emisorId(usuarioId)
                .emisorNombre(nombre)
                .tipo("SISTEMA")
                .contenidoTexto(nombre + " " + accion)
                .estado("ENVIADO")
                .createdAt(OffsetDateTime.now())
                .build());

        List<UUID> participantes = conversacionRepositoryPort.listarParticipantes(conversacionId).stream()
                .map(p -> p.getUserId())
                .filter(java.util.Objects::nonNull)
                .toList();
        chatNotificacionPort.mensajeSistema(MensajeResponseDto.builder()
                .id(guardado.getId())
                .conversacionId(guardado.getConversacionId())
                .emisorId(guardado.getEmisorId())
                .emisorNombre(guardado.getEmisorNombre())
                .tipo(guardado.getTipo())
                .contenidoTexto(guardado.getContenidoTexto())
                .estado(guardado.getEstado())
                .createdAt(guardado.getCreatedAt())
                .build(), participantes);
    }
}
