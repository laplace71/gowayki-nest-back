package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.AudioStoragePort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Elimina una conversación de verdad: fila de `conversacion`, sus participantes
 * y sus mensajes (la base los arrastra con ON DELETE CASCADE), más los audios
 * que esos mensajes tenían en disco.
 */
@Service
public class EliminarConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final AudioStoragePort audioStoragePort;

    public EliminarConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                       MensajeRepositoryPort mensajeRepositoryPort,
                                       AudioStoragePort audioStoragePort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.audioStoragePort = audioStoragePort;
    }

    @Transactional
    public void ejecutar(UUID conversacionId, UUID solicitanteId) {
        if (solicitanteId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }

        ConversacionModel conversacion = conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        verificarPermiso(conversacion, solicitanteId);

        // Los audios se buscan ANTES de borrar: después la fila ya no está.
        List<String> audios = mensajeRepositoryPort.findByConversacionId(conversacionId).stream()
                .map(MensajeModel::getAudioUrl)
                .filter(Objects::nonNull)
                .toList();

        conversacionRepositoryPort.eliminar(conversacionId);

        audios.forEach(audioStoragePort::eliminarAudio);
    }

    private void verificarPermiso(ConversacionModel conversacion, UUID solicitanteId) {
        boolean esCreador = conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(solicitanteId);

        if (esCreador) {
            return;
        }

        if (!conversacionRepositoryPort.existeParticipante(conversacion.getId(), solicitanteId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
        }

        if (!conversacionRepositoryPort.esAdministrador(conversacion.getId(), solicitanteId)) {
            throw new AccesoDenegadoException("Solo el creador o un administrador del grupo puede eliminarlo.");
        }
    }
}
