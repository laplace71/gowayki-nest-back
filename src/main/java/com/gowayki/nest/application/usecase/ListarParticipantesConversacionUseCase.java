package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ParticipanteResponseDto;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListarParticipantesConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public ListarParticipantesConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
    }

    public List<ParticipanteResponseDto> ejecutar(UUID conversacionId, UUID usuarioId) {
        conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        if (usuarioId != null && !conversacionRepositoryPort.existeParticipante(conversacionId, usuarioId)) {
            throw new IllegalArgumentException("No formas parte de esta conversación.");
        }

        return conversacionRepositoryPort.listarParticipantes(conversacionId).stream()
                .map(p -> ParticipanteResponseDto.builder()
                        .userId(p.getUserId())
                        .nombre(p.getNombre())
                        .rol(p.getRol())
                        .fotoUrl(p.getFotoUrl())
                        .isOnline(p.getIsOnline())
                        .build())
                .toList();
    }
}
