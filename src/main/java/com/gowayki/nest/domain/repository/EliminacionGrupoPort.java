package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.EliminacionGrupoModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EliminacionGrupoPort {
    Optional<EliminacionGrupoModel> buscarPendiente(UUID conversacionId);

    Optional<EliminacionGrupoModel> buscarPorId(UUID id);

    /** Lectura con bloqueo de fila para serializar cancelación y purga. */
    Optional<EliminacionGrupoModel> buscarPorIdParaActualizar(UUID id);

    EliminacionGrupoModel guardar(EliminacionGrupoModel model);

    /** Solicitudes pendientes cuyo plazo de 72 h ya venció. */
    List<EliminacionGrupoModel> pendientesVencidas(OffsetDateTime ahora);

    void marcarCancelada(UUID id, UUID usuarioId, OffsetDateTime momento);

    void marcarEjecutada(UUID id, OffsetDateTime momento);
}
