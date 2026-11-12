package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.application.dto.MensajeResponseDto;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListarConversacionesUsuarioUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;

    public ListarConversacionesUsuarioUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                            MensajeRepositoryPort mensajeRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
    }

    public List<ConversacionResponseDto> ejecutar(UUID usuarioId) {
        List<ConversacionModel> conversaciones = conversacionRepositoryPort.findByUsuarioId(usuarioId);

        return conversaciones.stream().map(c -> {
            int total = mensajeRepositoryPort.contarMensajes(c.getId());
            int participantesCount = conversacionRepositoryPort.contarParticipantes(c.getId());
            Optional<MensajeModel> ultimoOpt = mensajeRepositoryPort.findUltimoMensaje(c.getId());

            MensajeResponseDto ultimoDto = ultimoOpt.map(m -> MensajeResponseDto.builder()
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
                    .build()).orElse(null);

            return ConversacionResponseDto.builder()
                    .id(c.getId())
                    .titulo(c.getTitulo())
                    .esGrupo(c.getEsGrupo())
                    .createdBy(c.getCreatedBy())
                    .createdAt(c.getCreatedAt())
                    .totalMensajes(total)
                    .ultimoMensaje(ultimoDto)
                    .totalParticipantes(participantesCount)
                    .build();
        }).toList();
    }
}

