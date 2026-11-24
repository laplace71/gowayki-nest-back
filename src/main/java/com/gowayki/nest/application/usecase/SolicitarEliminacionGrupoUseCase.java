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
    private final MensajeRepositoryPort mensajeRepositoryPort;

    public SolicitarEliminacionGrupoUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                            EliminacionGrupoPort eliminacionGrupoPort,
                                            MensajeRepositoryPort mensajeRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
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
        return marcarParaEliminacion(conversacion, solicitanteId);
    }

    /**
     * Variante para depuración administrativa. La llama un ADMIN autenticado;
     * no exige ser creador ni miembro del grupo, pero aplica EXACTAMENTE el
     * mismo soft delete de 72 horas.
     */
    @Transactional
    public EliminacionGrupoModel ejecutarComoAdministrador(UUID conversacionId, UUID administradorId) {
        if (administradorId == null) {
            throw new NoAutenticadoException("Se requiere un administrador autenticado.");
        }
        ConversacionModel conversacion = conversacionRepositoryPort.bloquearParaActualizar(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));
        if (!Boolean.TRUE.equals(conversacion.getEsGrupo())) {
            throw new IllegalArgumentException("Solo se pueden eliminar grupos.");
        }
        return marcarParaEliminacion(conversacion, administradorId);
    }

    private EliminacionGrupoModel marcarParaEliminacion(ConversacionModel conversacion, UUID solicitanteId) {
        UUID conversacionId = conversacion.getId();
        // Idempotente: si ya hay una solicitud viva, se devuelve esa.
        EliminacionGrupoModel solicitud = eliminacionGrupoPort.buscarPendiente(conversacionId)
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

        // Soft delete: el grupo y sus mensajes dejan de verse YA en todas las
        // apps; las filas siguen en BD hasta la purga de las 72 horas.
        OffsetDateTime marcadoEn = solicitud.getSolicitadoEn() != null
                ? solicitud.getSolicitadoEn() : OffsetDateTime.now();
        conversacionRepositoryPort.marcarEliminado(conversacionId, marcadoEn);
        mensajeRepositoryPort.marcarEliminados(conversacionId, marcadoEn);

        return solicitud;
    }

    private void verificarPermiso(ConversacionModel conversacion, UUID solicitanteId) {
        if (!conversacionRepositoryPort.existeParticipante(conversacion.getId(), solicitanteId)) {
            throw new AccesoDenegadoException("No formas parte de este grupo.");
        }
        boolean esCreador = conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(solicitanteId);

        if (esCreador || conversacionRepositoryPort.esAdministrador(conversacion.getId(), solicitanteId)) {
            return;
        }
        throw new AccesoDenegadoException(
                "Solo el creador o un administrador del grupo puede pedir su eliminación definitiva.");
    }
}
