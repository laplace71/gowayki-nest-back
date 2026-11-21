package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * "Eliminar para mí": el mensaje desaparece SOLO para quien lo pide.
 *
 * Se guarda una marca por usuario en la BD (sobrevive a reinstalar la app y a
 * cambiar de dispositivo). No se borra nada, no se avisa a nadie y los demás
 * participantes siguen viendo el mensaje con normalidad.
 */
@Service
public class OcultarMensajeUseCase {

    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public OcultarMensajeUseCase(MensajeRepositoryPort mensajeRepositoryPort,
                                 ConversacionRepositoryPort conversacionRepositoryPort) {
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
    }

    @Transactional
    public void ejecutar(UUID conversacionId, UUID mensajeId, UUID usuarioId) {
        if (usuarioId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }

        MensajeModel mensaje = mensajeRepositoryPort.findById(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("El mensaje no existe."));

        if (!conversacionId.equals(mensaje.getConversacionId())) {
            throw new IllegalArgumentException("El mensaje no pertenece a esta conversación.");
        }
        if (!conversacionRepositoryPort.existeParticipante(conversacionId, usuarioId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
        }

        mensajeRepositoryPort.ocultarParaUsuario(mensajeId, usuarioId);
    }
}
