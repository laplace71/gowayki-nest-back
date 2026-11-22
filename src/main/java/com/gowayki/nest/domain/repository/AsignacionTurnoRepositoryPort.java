package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AsignacionTurnoRepositoryPort {
    AsignacionTurnoModel save(AsignacionTurnoModel model);
    Optional<AsignacionTurnoModel> findById(UUID id);
    Optional<AsignacionTurnoModel> findTurnoActivoPorChofer(UUID choferId);
    List<AsignacionTurnoModel> findByChoferId(UUID choferId);
    List<AsignacionTurnoModel> findTurnosActivos();

    /** Cambia la subruta concreta de un turno existente. */
    Optional<AsignacionTurnoModel> cambiarSubruta(UUID id, String subrutaCodigo);
}
