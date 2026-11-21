package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Permite que un administrador (o el creador) del grupo cambie el rol de otro
 * participante: ADMIN o MIEMBRO.
 */
@Service
public class AsignarRolParticipanteUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public AsignarRolParticipanteUseCase(ConversacionRepositoryPort conversacionRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
    }

    @Transactional
    public void ejecutar(UUID conversacionId, UUID solicitanteId, UUID objetivoId, String nuevoRol) {
        if (solicitanteId == null || objetivoId == null) {
            throw new NoAutenticadoException("Faltan datos para cambiar el rol.");
        }
        if (!"ADMIN".equals(nuevoRol) && !"MIEMBRO".equals(nuevoRol)) {
            throw new IllegalArgumentException("El rol debe ser ADMIN o MIEMBRO.");
        }

        ConversacionModel conversacion = conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        // El creador conserva el derecho solo mientras siga siendo participante:
        // quien salió del grupo no puede cambiar los roles de quienes quedan.
        if (!conversacionRepositoryPort.existeParticipante(conversacionId, solicitanteId)) {
            throw new AccesoDenegadoException("No formas parte de esta conversación.");
        }

        boolean solicitanteEsCreador = conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(solicitanteId);
        boolean solicitanteEsAdmin = conversacionRepositoryPort.esAdministrador(conversacionId, solicitanteId);
        if (!solicitanteEsCreador && !solicitanteEsAdmin) {
            throw new AccesoDenegadoException("Solo un administrador puede asignar roles.");
        }

        if (!conversacionRepositoryPort.existeParticipante(conversacionId, objetivoId)) {
            throw new AccesoDenegadoException("El usuario indicado no forma parte de esta conversación.");
        }

        // El creador conserva siempre su rol de administrador.
        if ("MIEMBRO".equals(nuevoRol)
                && conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(objetivoId)) {
            throw new IllegalArgumentException("El creador del grupo no puede dejar de ser administrador.");
        }

        conversacionRepositoryPort.cambiarRol(conversacionId, objetivoId, nuevoRol);
    }
}
