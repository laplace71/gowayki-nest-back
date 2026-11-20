package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ParticipanteResponseDto;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
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
        if (usuarioId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }
        conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        if (!conversacionRepositoryPort.existeParticipante(conversacionId, usuarioId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
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
