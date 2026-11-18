package com.gowayki.nest.application.usecase;

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
 * "Salir del grupo": saca al usuario de la conversación. El grupo y sus
 * mensajes siguen existiendo para los demás.
 *
 * Si el grupo se queda sin nadie, se borra entero (con sus mensajes y audios):
 * no tiene sentido conservar un grupo al que no pertenece nadie.
 */
@Service
public class SalirConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final AudioStoragePort audioStoragePort;

    public SalirConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                    MensajeRepositoryPort mensajeRepositoryPort,
                                    AudioStoragePort audioStoragePort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.audioStoragePort = audioStoragePort;
    }

    /**
     * @return true si la conversación quedó sin participantes y se eliminó por completo.
     */
    @Transactional
    public boolean ejecutar(UUID conversacionId, UUID usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Se requiere el usuario que sale del grupo.");
        }

        ConversacionModel conversacion = conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        if (!conversacionRepositoryPort.existeParticipante(conversacionId, usuarioId)) {
            throw new IllegalArgumentException("No formas parte de esta conversación.");
        }

        conversacionRepositoryPort.quitarParticipante(conversacionId, usuarioId);

        if (Boolean.TRUE.equals(conversacion.getEsGrupo())
                && conversacionRepositoryPort.contarParticipantes(conversacionId) == 0) {
            // Se quedó sin nadie: morir con dignidad, llevándose mensajes y audios.
            List<String> audios = mensajeRepositoryPort.findByConversacionId(conversacionId).stream()
                    .map(MensajeModel::getAudioUrl)
                    .filter(Objects::nonNull)
                    .toList();
            conversacionRepositoryPort.eliminar(conversacionId);
            audios.forEach(audioStoragePort::eliminarAudio);
            return true;
        }
        return false;
    }
}
