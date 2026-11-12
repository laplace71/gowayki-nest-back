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

        if (Boolean.TRUE.equals(request.getEsGrupo()) && request.getTitulo() != null && !request.getTitulo().isBlank() && request.getTitulo().trim().startsWith("GRUPO · ")) {
            java.util.Optional<ConversacionModel> existenteOpt = conversacionRepositoryPort.findByTitulo(request.getTitulo().trim());
            if (existenteOpt.isPresent()) {
                ConversacionModel existente = existenteOpt.get();
                if (request.getCreatedBy() != null) {
                    conversacionRepositoryPort.agregarParticipante(existente.getId(), request.getCreatedBy());
                }
                if (request.getParticipanteIds() != null) {
                    for (UUID pId : request.getParticipanteIds()) {
                        conversacionRepositoryPort.agregarParticipante(existente.getId(), pId);
                    }
                }
                int count = conversacionRepositoryPort.contarParticipantes(existente.getId());
                return ConversacionResponseDto.builder()
                        .id(existente.getId())
                        .titulo(existente.getTitulo())
                        .esGrupo(existente.getEsGrupo())
                        .createdBy(existente.getCreatedBy())
                        .createdAt(existente.getCreatedAt())
                        .totalMensajes(0)
                        .ultimoMensaje(null)
                        .totalParticipantes(count)
                        .build();
            }
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
        int count = conversacionRepositoryPort.contarParticipantes(guardado.getId());

        return ConversacionResponseDto.builder()
                .id(guardado.getId())
                .titulo(guardado.getTitulo())
                .esGrupo(guardado.getEsGrupo())
                .createdBy(guardado.getCreatedBy())
                .createdAt(guardado.getCreatedAt())
                .totalMensajes(0)
                .ultimoMensaje(null)
                .totalParticipantes(count)
                .build();
    }
}

