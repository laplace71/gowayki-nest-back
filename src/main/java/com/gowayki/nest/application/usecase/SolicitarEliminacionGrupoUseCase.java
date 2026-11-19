package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pide la eliminación definitiva de un grupo. No borra nada todavía: registra la
 * solicitud y deja 72 horas de espera antes de que la purga la ejecute.
 *
 * Quién puede pedirla: la misma regla que ya existía para borrar un grupo
 * (creador o administrador). No se inventan permisos nuevos.
 */
@Service
public class SolicitarEliminacionGrupoUseCase {

    public static final int HORAS_DE_ESPERA = 72;

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;

    public SolicitarEliminacionGrupoUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                            EliminacionGrupoPort eliminacionGrupoPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
    }

    @Transactional
    public EliminacionGrupoModel ejecutar(UUID conversacionId, UUID solicitanteId) {
        if (solicitanteId == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }

        ConversacionModel conversacion = conversacionRepositoryPort.bloquearParaActualizar(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        if (!Boolean.TRUE.equals(conversacion.getEsGrupo())) {
            throw new IllegalArgumentException(
                    "Solo se puede eliminar definitivamente un grupo. Para un chat privado usa ocultar.");
        }

        verificarPermiso(conversacion, solicitanteId);

        // Idempotente: si ya hay una solicitud viva, se devuelve esa.
        return eliminacionGrupoPort.buscarPendiente(conversacionId)
                .orElseGet(() -> {
                    OffsetDateTime ahora = OffsetDateTime.now();
                    return eliminacionGrupoPort.guardar(EliminacionGrupoModel.builder()
                            .conversacionId(conversacionId)
                            .tituloSnapshot(conversacion.getTitulo())
                            .solicitadoPor(solicitanteId)
                            .solicitadoEn(ahora)
                            .ejecutarDespues(ahora.plusHours(HORAS_DE_ESPERA))
                            .estado("PENDIENTE")
                            .build());
                });
    }

    private void verificarPermiso(ConversacionModel conversacion, UUID solicitanteId) {
        boolean esCreador = conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(solicitanteId);

        if (esCreador || conversacionRepositoryPort.esAdministrador(conversacion.getId(), solicitanteId)) {
            return;
        }
        throw new AccesoDenegadoException(
                "Solo el creador o un administrador del grupo puede pedir su eliminación definitiva.");
    }
}
