package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.application.dto.CrearConversacionRequestDto;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.AudioStoragePort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class CrearConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final AudioStoragePort audioStoragePort;

    public CrearConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                   UsuarioRepositoryPort usuarioRepositoryPort,
                                   MensajeRepositoryPort mensajeRepositoryPort,
                                   AudioStoragePort audioStoragePort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.audioStoragePort = audioStoragePort;
    }

    @Transactional
    public ConversacionResponseDto ejecutar(CrearConversacionRequestDto request) {
        if (request.getCreatedBy() != null && usuarioRepositoryPort.findById(request.getCreatedBy()).isEmpty()) {
            throw new IllegalArgumentException("El usuario creador no existe.");
        }

        boolean esGrupoRuta = Boolean.TRUE.equals(request.getEsGrupo())
                && request.getTitulo() != null
                && !request.getTitulo().isBlank()
                && request.getTitulo().trim().startsWith("GRUPO · ");

        if (esGrupoRuta) {
            java.util.Optional<ConversacionModel> existenteOpt =
                    conversacionRepositoryPort.findByTitulo(request.getTitulo().trim());
            if (existenteOpt.isPresent()) {
                ConversacionModel existente = existenteOpt.get();
                Set<UUID> entrando = usuariosEntrando(request);
                for (UUID pId : entrando) {
                    conversacionRepositoryPort.agregarParticipante(existente.getId(), pId);
                }
                for (UUID pId : entrando) {
                    salirDeOtrosGruposRuta(pId, existente.getId());
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

        List<UUID> participantes = new ArrayList<>(usuariosEntrando(request));

        ConversacionModel model = ConversacionModel.builder()
                .titulo(request.getTitulo() != null ? request.getTitulo().trim() : null)
                .esGrupo(Boolean.TRUE.equals(request.getEsGrupo()))
                .createdBy(request.getCreatedBy())
                .createdAt(OffsetDateTime.now())
                .build();

        ConversacionModel guardado = conversacionRepositoryPort.save(model, participantes);

        if (esGrupoRuta) {
            for (UUID pId : participantes) {
                salirDeOtrosGruposRuta(pId, guardado.getId());
            }
        }

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

    private Set<UUID> usuariosEntrando(CrearConversacionRequestDto request) {
        Set<UUID> ids = new LinkedHashSet<>();
        if (request.getParticipanteIds() != null) {
            ids.addAll(request.getParticipanteIds());
        }
        if (request.getCreatedBy() != null) {
            ids.add(request.getCreatedBy());
        }
        return ids;
    }

    /**
     * Al entrar a un grupo de ruta (subruta), el usuario deja de pertenecer a los
     * demás grupos de ruta: así cada subruta solo tiene a quienes están en ella.
     * Si algún grupo queda vacío, se elimina junto con sus mensajes y audios.
     */
    private void salirDeOtrosGruposRuta(UUID usuarioId, UUID grupoActualId) {
        if (usuarioId == null) return;
        for (ConversacionModel c : conversacionRepositoryPort.findByUsuarioId(usuarioId)) {
            if (c.getId() == null || c.getId().equals(grupoActualId)) continue;
            String titulo = c.getTitulo();
            if (!Boolean.TRUE.equals(c.getEsGrupo()) || titulo == null || !titulo.startsWith("GRUPO · ")) continue;

            try {
                conversacionRepositoryPort.quitarParticipante(c.getId(), usuarioId);
            } catch (IllegalArgumentException ignored) {
                // Ya no era participante; nada que hacer.
            }

            if (conversacionRepositoryPort.contarParticipantes(c.getId()) == 0) {
                List<String> audios = mensajeRepositoryPort.findByConversacionId(c.getId()).stream()
                        .map(MensajeModel::getAudioUrl)
                        .filter(Objects::nonNull)
                        .toList();
                conversacionRepositoryPort.eliminar(c.getId());
                audios.forEach(audioStoragePort::eliminarAudio);
            }
        }
    }
}

