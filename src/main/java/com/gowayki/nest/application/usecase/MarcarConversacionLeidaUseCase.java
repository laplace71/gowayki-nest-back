package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * El chofer abrió el chat: los mensajes de los demás dejan de contar como
 * no leídos. El estado es compartido por la conversación (el modelo actual no
 * tiene estado de lectura por usuario), pero evita que el badge reaparezca
 * después de refrescar desde el backend.
 */
@Service
public class MarcarConversacionLeidaUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;

    public MarcarConversacionLeidaUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                          MensajeRepositoryPort mensajeRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
    }

    @Transactional
    public void ejecutar(UUID conversacionId, UUID usuarioId) {
        if (usuarioId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }
        if (!conversacionRepositoryPort.existeParticipante(conversacionId, usuarioId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
        }
        mensajeRepositoryPort.marcarLeidosParaUsuario(conversacionId, usuarioId);
    }
}
