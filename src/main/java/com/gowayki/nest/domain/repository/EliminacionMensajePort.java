package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.EliminacionMensajeModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EliminacionMensajePort {

    Optional<EliminacionMensajeModel> buscarPorId(UUID id);

    /** Ya hay una purga pendiente para este mensaje (la petición es idempotente). */
    Optional<EliminacionMensajeModel> buscarPendiente(UUID mensajeId);

    EliminacionMensajeModel guardar(EliminacionMensajeModel model);

    /** Solicitudes pendientes cuyo plazo de 72 h ya venció. */
    List<EliminacionMensajeModel> pendientesVencidas(OffsetDateTime ahora);

    void marcarEjecutada(UUID id, OffsetDateTime momento);
}
