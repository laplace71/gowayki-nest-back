package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * "Limpiar" un chat es una acción PURAMENTE VISUAL y PERSONAL (regla 1).
 *
 * Guarda una marca de tiempo para ese usuario; no borra mensajes, no cambia la
 * membresía y los demás participantes no notan nada. Si luego llega un mensaje
 * nuevo, la conversación vuelve a aparecer y solo se muestran los posteriores.
 */
@Service
public class LimpiarConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public LimpiarConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort) {
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
        conversacionRepositoryPort.limpiarParaUsuario(conversacionId, usuarioId, OffsetDateTime.now());
    }
}
