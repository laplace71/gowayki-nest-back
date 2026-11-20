package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.EliminacionMensajeModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ArchivoPendienteBorradoPort;
import com.gowayki.nest.domain.repository.EliminacionMensajePort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Purga física de un mensaje cuyo plazo de retención (72 h) venció.
 *
 * El mensaje ya estaba oculto para todos desde que se pidió, así que aquí no
 * hay nada que avisar: solo se borra la fila y se encola su audio para que el
 * job de archivos lo elimine después del commit.
 */
@Service
public class EjecutarEliminacionMensajeUseCase {

    private final EliminacionMensajePort eliminacionMensajePort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ArchivoPendienteBorradoPort archivoPendienteBorradoPort;

    public EjecutarEliminacionMensajeUseCase(EliminacionMensajePort eliminacionMensajePort,
                                             MensajeRepositoryPort mensajeRepositoryPort,
                                             ArchivoPendienteBorradoPort archivoPendienteBorradoPort) {
        this.eliminacionMensajePort = eliminacionMensajePort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.archivoPendienteBorradoPort = archivoPendienteBorradoPort;
    }

    @Transactional
    public Optional<UUID> ejecutar(UUID solicitudId) {
        Optional<EliminacionMensajeModel> encontrada = eliminacionMensajePort.buscarPorId(solicitudId);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }

        EliminacionMensajeModel solicitud = encontrada.get();
        OffsetDateTime ahora = OffsetDateTime.now();
        if (!"PENDIENTE".equals(solicitud.getEstado()) || solicitud.getEjecutarDespues().isAfter(ahora)) {
            return Optional.empty();
        }

        UUID mensajeId = solicitud.getMensajeId();
        String audioUrl = mensajeRepositoryPort.findById(mensajeId)
                .map(MensajeModel::getAudioUrl)
                .orElse(null);

        eliminacionMensajePort.marcarEjecutada(solicitudId, ahora);
        mensajeRepositoryPort.eliminarFisicamente(mensajeId);

        if (audioUrl != null && !audioUrl.isBlank()) {
            archivoPendienteBorradoPort.encolar(audioUrl, "MENSAJE_ELIMINADO");
        }
        return Optional.of(mensajeId);
    }
}
