package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.EliminacionMensajeModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionMensajePort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * "Eliminar para todos": desaparece YA para todos los participantes.
 *
 * Solo puede hacerlo quien envió el mensaje (la identidad sale del JWT, nunca
 * del cliente). El mensaje queda marcado en la BD y un job lo purga a las 72
 * horas junto con su audio. No hay cancelación y al usuario no se le informa
 * el plazo: para él el mensaje simplemente ya no existe.
 */
@Service
public class EliminarMensajeParaTodosUseCase {

    static final int HORAS_DE_RETENCION = 72;

    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final EliminacionMensajePort eliminacionMensajePort;

    public EliminarMensajeParaTodosUseCase(MensajeRepositoryPort mensajeRepositoryPort,
                                           ConversacionRepositoryPort conversacionRepositoryPort,
                                           EliminacionMensajePort eliminacionMensajePort) {
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.eliminacionMensajePort = eliminacionMensajePort;
    }

    /** @return participantes a los que hay que avisar por WebSocket. */
    @Transactional
    public List<UUID> ejecutar(UUID conversacionId, UUID mensajeId, UUID solicitanteId) {
        if (solicitanteId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }

        MensajeModel mensaje = mensajeRepositoryPort.findById(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("El mensaje no existe."));

        if (!conversacionId.equals(mensaje.getConversacionId())) {
            throw new IllegalArgumentException("El mensaje no pertenece a esta conversación.");
        }
        if (!conversacionRepositoryPort.existeParticipante(conversacionId, solicitanteId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
        }
        if (!solicitanteId.equals(mensaje.getEmisorId())) {
            throw new AccesoDenegadoException("Solo quien envió el mensaje puede eliminarlo para todos.");
        }

        List<UUID> participantes = conversacionRepositoryPort.listarParticipantes(conversacionId).stream()
                .map(participante -> participante.getUserId())
                .filter(Objects::nonNull)
                .toList();

        // Idempotente: dos pulsaciones (o dos dispositivos) no duplican la purga.
        if (eliminacionMensajePort.buscarPendiente(mensajeId).isPresent()) {
            return participantes;
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        mensajeRepositoryPort.marcarEliminado(mensajeId, ahora);

        eliminacionMensajePort.guardar(EliminacionMensajeModel.builder()
                .mensajeId(mensajeId)
                .conversacionId(conversacionId)
                .solicitadoPor(solicitanteId)
                .solicitadoEn(ahora)
                .ejecutarDespues(ahora.plusHours(HORAS_DE_RETENCION))
                .estado("PENDIENTE")
                .build());

        return participantes;
    }
}
