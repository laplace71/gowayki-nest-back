package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ArchivoPendienteBorradoPort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Ejecuta una solicitud de eliminación definitiva ya vencida.
 *
 * Vuelve a comprobar todo antes de borrar: que siga pendiente, que el plazo
 * pasó, que la conversación existe y que quien la pidió sigue teniendo
 * permiso. Si algo cambió, la solicitud se cancela y no se borra nada.
 */
@Service
public class EjecutarEliminacionGrupoUseCase {

    /** Lo que hay que avisar por WebSocket una vez confirmado el commit. */
    public record Resultado(UUID conversacionId, List<UUID> participantes) {
    }

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;
    private final ArchivoPendienteBorradoPort archivoPendienteBorradoPort;

    public EjecutarEliminacionGrupoUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                           MensajeRepositoryPort mensajeRepositoryPort,
                                           EliminacionGrupoPort eliminacionGrupoPort,
                                           ArchivoPendienteBorradoPort archivoPendienteBorradoPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.archivoPendienteBorradoPort = archivoPendienteBorradoPort;
    }

    @Transactional
    public Optional<Resultado> ejecutar(UUID solicitudId) {
        Optional<EliminacionGrupoModel> encontrada = eliminacionGrupoPort.buscarPorId(solicitudId);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }

        EliminacionGrupoModel solicitud = encontrada.get();
        OffsetDateTime ahora = OffsetDateTime.now();

        if (!"PENDIENTE".equals(solicitud.getEstado()) || solicitud.getEjecutarDespues().isAfter(ahora)) {
            return Optional.empty();
        }

        UUID conversacionId = solicitud.getConversacionId();
        if (conversacionId == null) {
            // La conversación ya no existe (la borró otra cosa): se cierra la solicitud.
            eliminacionGrupoPort.marcarEjecutada(solicitud.getId(), ahora);
            return Optional.empty();
        }

        Optional<ConversacionModel> conversacionOpt = conversacionRepositoryPort.findById(conversacionId);
        if (conversacionOpt.isEmpty()) {
            eliminacionGrupoPort.marcarEjecutada(solicitud.getId(), ahora);
            return Optional.empty();
        }

        ConversacionModel conversacion = conversacionOpt.get();
        if (!sigueAutorizado(conversacion, solicitud.getSolicitadoPor())) {
            // Perdió el permiso (dejó de ser admin o de participar): no se borra.
            eliminacionGrupoPort.marcarCancelada(solicitud.getId(), null, ahora);
            return Optional.empty();
        }

        List<UUID> participantes = conversacionRepositoryPort.listarParticipantes(conversacionId).stream()
                .map(participante -> participante.getUserId())
                .filter(Objects::nonNull)
                .toList();

        List<String> audios = mensajeRepositoryPort.findByConversacionId(conversacionId).stream()
                .map(MensajeModel::getAudioUrl)
                .filter(Objects::nonNull)
                .toList();

        // Primero se cierra la solicitud (desvinculándola de la conversación) y
        // después se borra: así la FK no queda apuntando a una fila eliminada.
        eliminacionGrupoPort.marcarEjecutada(solicitud.getId(), ahora);
        conversacionRepositoryPort.eliminar(conversacionId);

        // Los archivos se borran después del commit, nunca dentro de la transacción.
        audios.forEach(ruta -> archivoPendienteBorradoPort.encolar(ruta, "GRUPO_ELIMINADO"));

        return Optional.of(new Resultado(conversacionId, participantes));
    }

    private boolean sigueAutorizado(ConversacionModel conversacion, UUID solicitanteId) {
        if (solicitanteId == null) {
            return false;
        }
        // Quien pidió el borrado debe seguir dentro del grupo: si se fue, ya no
        // tiene nada que decidir sobre él.
        if (!conversacionRepositoryPort.existeParticipante(conversacion.getId(), solicitanteId)) {
            return false;
        }
        boolean esCreador = conversacion.getCreatedBy() != null
                && conversacion.getCreatedBy().equals(solicitanteId);
        return esCreador || conversacionRepositoryPort.esAdministrador(conversacion.getId(), solicitanteId);
    }
}
