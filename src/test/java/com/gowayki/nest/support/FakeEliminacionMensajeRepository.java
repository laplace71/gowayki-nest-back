package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.EliminacionMensajeModel;
import com.gowayki.nest.domain.repository.EliminacionMensajePort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FakeEliminacionMensajeRepository implements EliminacionMensajePort {

    public final Map<UUID, EliminacionMensajeModel> solicitudes = new LinkedHashMap<>();

    @Override
    public Optional<EliminacionMensajeModel> buscarPorId(UUID id) {
        return Optional.ofNullable(solicitudes.get(id));
    }

    @Override
    public Optional<EliminacionMensajeModel> buscarPendiente(UUID mensajeId) {
        return solicitudes.values().stream()
                .filter(s -> mensajeId.equals(s.getMensajeId()) && "PENDIENTE".equals(s.getEstado()))
                .findFirst();
    }

    @Override
    public EliminacionMensajeModel guardar(EliminacionMensajeModel model) {
        if (model.getId() == null) {
            model.setId(UUID.randomUUID());
        }
        solicitudes.put(model.getId(), model);
        return model;
    }

    @Override
    public List<EliminacionMensajeModel> pendientesVencidas(OffsetDateTime ahora) {
        return solicitudes.values().stream()
                .filter(s -> "PENDIENTE".equals(s.getEstado()))
                .filter(s -> !s.getEjecutarDespues().isAfter(ahora))
                .toList();
    }

    @Override
    public void marcarEjecutada(UUID id, OffsetDateTime momento) {
        EliminacionMensajeModel s = solicitudes.get(id);
        if (s != null) {
            s.setEstado("EJECUTADA");
            s.setEjecutadaEn(momento);
        }
    }
}
