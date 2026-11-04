package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.application.dto.CrearConversacionRequestDto;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CrearConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public CrearConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                   UsuarioRepositoryPort usuarioRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public ConversacionResponseDto ejecutar(CrearConversacionRequestDto request) {
        if (request.getCreatedBy() != null && usuarioRepositoryPort.findById(request.getCreatedBy()).isEmpty()) {
            throw new IllegalArgumentException("El usuario creador no existe.");
        }

        List<UUID> participantes = new ArrayList<>();
        if (request.getParticipanteIds() != null) {
            participantes.addAll(request.getParticipanteIds());
        }
        if (request.getCreatedBy() != null && !participantes.contains(request.getCreatedBy())) {
            participantes.add(request.getCreatedBy());
        }

        ConversacionModel model = ConversacionModel.builder()
                .titulo(request.getTitulo())
                .esGrupo(Boolean.TRUE.equals(request.getEsGrupo()))
                .createdBy(request.getCreatedBy())
                .createdAt(OffsetDateTime.now())
                .build();

        ConversacionModel guardado = conversacionRepositoryPort.save(model, participantes);

        return ConversacionResponseDto.builder()
                .id(guardado.getId())
                .titulo(guardado.getTitulo())
                .esGrupo(guardado.getEsGrupo())
                .createdBy(guardado.getCreatedBy())
                .createdAt(guardado.getCreatedAt())
                .totalMensajes(0)
                .ultimoMensaje(null)
                .build();
    }
}
