package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Cola de eliminaciones en memoria, con la misma semántica que el adaptador. */
public class FakeEliminacionGrupoRepository implements EliminacionGrupoPort {

    public final Map<UUID, EliminacionGrupoModel> solicitudes = new LinkedHashMap<>();
    public final List<String> llamadas = new ArrayList<>();

    public UUID agregarPendiente(UUID conversacionId, UUID solicitadoPor, OffsetDateTime ejecutarDespues) {
        UUID id = UUID.randomUUID();
        solicitudes.put(id, EliminacionGrupoModel.builder()
                .id(id)
                .conversacionId(conversacionId)
                .solicitadoPor(solicitadoPor)
                .solicitadoEn(ejecutarDespues.minusHours(72))
                .ejecutarDespues(ejecutarDespues)
                .estado("PENDIENTE")
                .build());
        return id;
    }

    @Override
    public Optional<EliminacionGrupoModel> buscarPendiente(UUID conversacionId) {
        return solicitudes.values().stream()
                .filter(s -> "PENDIENTE".equals(s.getEstado()))
                .filter(s -> conversacionId != null && conversacionId.equals(s.getConversacionId()))
                .findFirst();
    }

    @Override
    public Optional<EliminacionGrupoModel> buscarPorId(UUID id) {
        return Optional.ofNullable(solicitudes.get(id));
    }

    @Override
    public EliminacionGrupoModel guardar(EliminacionGrupoModel model) {
        llamadas.add("guardar");
        UUID id = model.getId() != null ? model.getId() : UUID.randomUUID();
        model.setId(id);
        solicitudes.put(id, model);
        return model;
    }

    @Override
    public List<EliminacionGrupoModel> pendientesVencidas(OffsetDateTime ahora) {
        return solicitudes.values().stream()
                .filter(s -> "PENDIENTE".equals(s.getEstado()))
                .filter(s -> s.getEjecutarDespues() != null && s.getEjecutarDespues().isBefore(ahora))
                .toList();
    }

    @Override
    public void marcarCancelada(UUID id, UUID usuarioId, OffsetDateTime momento) {
        llamadas.add("marcarCancelada");
        Optional.ofNullable(solicitudes.get(id)).ifPresent(s -> {
            s.setEstado("CANCELADA");
            s.setCanceladaPor(usuarioId);
            s.setCanceladaEn(momento);
        });
    }

    @Override
    public void marcarEjecutada(UUID id, OffsetDateTime momento) {
        llamadas.add("marcarEjecutada");
        Optional.ofNullable(solicitudes.get(id)).ifPresent(s -> {
            s.setEstado("EJECUTADA");
            s.setEjecutadaEn(momento);
            s.setConversacionId(null);
        });
    }
}
