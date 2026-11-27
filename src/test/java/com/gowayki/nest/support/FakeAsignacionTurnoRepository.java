package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Turnos en memoria para probar la sincronización de grupos de ruta. */
public class FakeAsignacionTurnoRepository implements AsignacionTurnoRepositoryPort {

    private final Map<UUID, AsignacionTurnoModel> turnos = new LinkedHashMap<>();

    public UUID agregar(UUID choferId, String subrutaCodigo, String subrutaNombre) {
        UUID id = UUID.randomUUID();
        turnos.put(id, AsignacionTurnoModel.builder()
                .id(id)
                .choferId(choferId)
                .unidadId(1)
                .subrutaCodigo(subrutaCodigo)
                .subrutaNombre(subrutaNombre)
                .estado("EN_RUTA")
                .build());
        return id;
    }

    @Override
    public AsignacionTurnoModel save(AsignacionTurnoModel model) {
        if (model.getId() == null) {
            model.setId(UUID.randomUUID());
        }
        turnos.put(model.getId(), model);
        return model;
    }

    @Override
    public Optional<AsignacionTurnoModel> findById(UUID id) {
        return Optional.ofNullable(turnos.get(id));
    }

    @Override
    public Optional<AsignacionTurnoModel> findTurnoActivoPorChofer(UUID choferId) {
        return turnos.values().stream()
                .filter(t -> choferId.equals(t.getChoferId()))
                .filter(t -> "EN_RUTA".equals(t.getEstado()) || "PROGRAMADO".equals(t.getEstado()))
                .findFirst();
    }

    @Override
    public List<AsignacionTurnoModel> findByChoferId(UUID choferId) {
        return turnos.values().stream()
                .filter(t -> choferId.equals(t.getChoferId()))
                .toList();
    }

    @Override
    public List<AsignacionTurnoModel> findTurnosActivos() {
        return new ArrayList<>(turnos.values());
    }

    @Override
    public Optional<AsignacionTurnoModel> cambiarSubruta(UUID id, String subrutaCodigo) {
        AsignacionTurnoModel turno = turnos.get(id);
        if (turno == null || !"EN_RUTA".equals(turno.getEstado())) {
            return Optional.empty();
        }
        turno.setSubrutaCodigo(subrutaCodigo);
        turno.setSubrutaNombre("Subruta " + subrutaCodigo);
        return Optional.of(turno);
    }
}
