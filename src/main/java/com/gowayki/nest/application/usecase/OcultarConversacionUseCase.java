package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Oculta una conversación SOLO para quien lo pide.
 *
 * No borra mensajes, ni audios, ni la conversación; no toca la membresía ni los
 * roles y no inicia ningún plazo de eliminación. Los demás participantes siguen
 * viéndola y usándola igual que antes.
 */
@Service
public class OcultarConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public OcultarConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
    }

    @Transactional
    public void ejecutar(UUID conversacionId, UUID usuarioId) {
        if (usuarioId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }

        conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        if (!conversacionRepositoryPort.existeParticipante(conversacionId, usuarioId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
        }

        conversacionRepositoryPort.ocultarParaUsuario(conversacionId, usuarioId);
    }
}
