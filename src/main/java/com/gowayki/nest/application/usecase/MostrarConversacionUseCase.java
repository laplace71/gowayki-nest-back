package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Devuelve a la lista una conversación que el usuario había ocultado.
 * Idempotente: si no estaba oculta, no hace nada.
 */
@Service
public class MostrarConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public MostrarConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort) {
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

        conversacionRepositoryPort.mostrarParaUsuario(conversacionId, usuarioId);
    }
}
