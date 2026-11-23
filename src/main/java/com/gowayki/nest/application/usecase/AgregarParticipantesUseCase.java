package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.ConflictoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Invita conductores a una conversación ya existente, identificada por su id.
 *
 * A diferencia de "crear conversación", este endpoint funciona para grupos
 * normales y de ruta: no depende de que el backend reutilice un grupo por su
 * título o clave. Solo creador o ADMIN pueden invitar.
 */
@Service
public class AgregarParticipantesUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;
    private final MensajeSistemaService mensajeSistemaService;

    public AgregarParticipantesUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                       EliminacionGrupoPort eliminacionGrupoPort,
                                       MensajeSistemaService mensajeSistemaService) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.mensajeSistemaService = mensajeSistemaService;
    }

    @Transactional
    public void ejecutar(UUID conversacionId, UUID solicitanteId, List<UUID> nuevosIds) {
        if (solicitanteId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }
        if (nuevosIds == null || nuevosIds.isEmpty()) {
            return;
        }

        ConversacionModel conversacion = conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        boolean esCreador = conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(solicitanteId);
        if (!conversacionRepositoryPort.existeParticipante(conversacionId, solicitanteId)
                || (!esCreador && !conversacionRepositoryPort.esAdministrador(conversacionId, solicitanteId))) {
            throw new AccesoDenegadoException("Solo el creador o un administrador puede invitar miembros.");
        }
        if (eliminacionGrupoPort.buscarPendiente(conversacionId).isPresent()) {
            throw new ConflictoException("Este grupo está pendiente de eliminación; no admite nuevos miembros.");
        }

        for (UUID id : nuevosIds) {
            if (id == null) continue;
            boolean yaEraParticipante = conversacionRepositoryPort.existeParticipante(conversacionId, id);
            conversacionRepositoryPort.agregarParticipante(conversacionId, id);
            if (!yaEraParticipante) {
                mensajeSistemaService.avisar(conversacionId, id, "se unió al grupo");
            }
        }
    }
}
