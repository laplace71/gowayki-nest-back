package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.application.dto.MensajeResponseDto;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListarConversacionesUsuarioUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;

    public ListarConversacionesUsuarioUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                             MensajeRepositoryPort mensajeRepositoryPort,
                                             EliminacionGrupoPort eliminacionGrupoPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
    }

    public List<ConversacionResponseDto> ejecutar(UUID usuarioId) {
        // El repositorio ya excluye las que este usuario ocultó: ocultar es
        // personal y no afecta a los demás participantes.
        List<ConversacionModel> conversaciones = conversacionRepositoryPort.findByUsuarioId(usuarioId);

        return conversaciones.stream().map(c -> {
            // El resumen respeta lo mismo que el historial: mensajes limpiados
            // u ocultos para este usuario no deben aparecer como preview.
            int total = mensajeRepositoryPort.contarMensajes(c.getId(), usuarioId);
            int participantesCount = conversacionRepositoryPort.contarParticipantes(c.getId());
            Optional<MensajeModel> ultimoOpt = mensajeRepositoryPort.findUltimoMensaje(c.getId(), usuarioId);
            Optional<EliminacionGrupoModel> eliminacionOpt = eliminacionGrupoPort.buscarPendiente(c.getId());

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
                    .tipoConversacion(c.getTipoConversacion())
                    .claveRuta(c.getClaveRuta())
                    .createdBy(c.getCreatedBy())
                    .createdAt(c.getCreatedAt())
                    .totalMensajes(total)
                    .ultimoMensaje(ultimoDto)
                    .totalParticipantes(participantesCount)
                    .miRol(conversacionRepositoryPort.obtenerRol(c.getId(), usuarioId))
                    .eliminacionPendiente(eliminacionOpt.isPresent())
                    .eliminacionProgramadaPara(eliminacionOpt.map(EliminacionGrupoModel::getEjecutarDespues).orElse(null))
                    .build();
        }).toList();
    }
}
