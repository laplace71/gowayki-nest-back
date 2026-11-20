package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.MensajeResponseDto;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ObtenerMensajesUseCase {

    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public ObtenerMensajesUseCase(MensajeRepositoryPort mensajeRepositoryPort,
                                  ConversacionRepositoryPort conversacionRepositoryPort) {
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
    }

    public List<MensajeResponseDto> ejecutar(UUID conversacionId, UUID solicitanteId) {
        if (solicitanteId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }
        if (conversacionRepositoryPort.findById(conversacionId).isEmpty()) {
            throw new IllegalArgumentException("La conversación no existe.");
        }
        if (!conversacionRepositoryPort.existeParticipante(conversacionId, solicitanteId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
        }

        List<MensajeModel> mensajes = mensajeRepositoryPort.findVisiblesParaUsuario(conversacionId, solicitanteId);

        return mensajes.stream()
                .map(m -> MensajeResponseDto.builder()
                        .id(m.getId())
                        .conversacionId(m.getConversacionId())
                        .emisorId(m.getEmisorId())
                        .emisorNombre(m.getEmisorNombre())
                        .tipo(m.getTipo())
                        .contenidoTexto(m.getContenidoTexto())
                        .audioUrl(m.getAudioUrl())
                        .audioDuracionSegundos(m.getAudioDuracionSegundos())
                        .estado(m.getEstado())
                        .createdAt(m.getCreatedAt())
                        .build())
                .toList();
    }
}

