package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Cancela una solicitud de eliminación definitiva antes de que venza el plazo.
 * Solo puede hacerlo quien podría haberla pedido: creador o administrador.
 */
@Service
public class CancelarEliminacionGrupoUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;

    public CancelarEliminacionGrupoUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                           EliminacionGrupoPort eliminacionGrupoPort,
                                           MensajeRepositoryPort mensajeRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
    }

    @Transactional
    public void ejecutar(UUID conversacionId, UUID solicitanteId) {
        if (solicitanteId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }

        ConversacionModel conversacion = conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        EliminacionGrupoModel solicitud = eliminacionGrupoPort.buscarPendiente(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("Este grupo no tiene una eliminación pendiente."));

        boolean esCreador = conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(solicitanteId);
        boolean esAdmin = conversacionRepositoryPort.esAdministrador(conversacionId, solicitanteId);
        if (!conversacionRepositoryPort.existeParticipante(conversacionId, solicitanteId)
                || (!esCreador && !esAdmin)) {
            throw new AccesoDenegadoException(
                    "Solo el creador o un administrador del grupo puede cancelar la eliminación.");
        }

        eliminacionGrupoPort.marcarCancelada(solicitud.getId(), solicitanteId, OffsetDateTime.now());

        // Se deshace el soft delete: el grupo y sus mensajes vuelven a verse.
        conversacionRepositoryPort.desmarcarEliminado(conversacionId);
        mensajeRepositoryPort.desmarcarEliminados(conversacionId);
    }
}
