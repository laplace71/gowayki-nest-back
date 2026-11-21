package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import com.gowayki.nest.infrastructure.persistence.entity.EliminacionGrupo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EliminacionGrupoPersistenceAdapter implements EliminacionGrupoPort {

    private final SpringDataEliminacionGrupoRepository repository;

    public EliminacionGrupoPersistenceAdapter(SpringDataEliminacionGrupoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EliminacionGrupoModel> buscarPendiente(UUID conversacionId) {
        return repository
                .findByConversacionIdAndEstado(conversacionId, EliminacionGrupo.PENDIENTE)
                .map(this::toDomain);
    }

    @Override
    public Optional<EliminacionGrupoModel> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<EliminacionGrupoModel> buscarPorIdParaActualizar(UUID id) {
        return repository.findByIdForUpdate(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public EliminacionGrupoModel guardar(EliminacionGrupoModel model) {
        EliminacionGrupo entity = EliminacionGrupo.builder()
                .id(model.getId())
                .conversacionId(model.getConversacionId())
                .tituloSnapshot(model.getTituloSnapshot())
                .solicitadoPor(model.getSolicitadoPor())
                .solicitadoEn(model.getSolicitadoEn() == null ? OffsetDateTime.now() : model.getSolicitadoEn())
                .ejecutarDespues(model.getEjecutarDespues())
                .estado(model.getEstado() == null ? EliminacionGrupo.PENDIENTE : model.getEstado())
                .canceladaPor(model.getCanceladaPor())
                .canceladaEn(model.getCanceladaEn())
                .ejecutadaEn(model.getEjecutadaEn())
                .build();
        return toDomain(repository.save(entity));
    }

    @Override
    public List<EliminacionGrupoModel> pendientesVencidas(OffsetDateTime ahora) {
        return repository
                .findByEstadoAndEjecutarDespuesBefore(EliminacionGrupo.PENDIENTE, ahora)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void marcarCancelada(UUID id, UUID usuarioId, OffsetDateTime momento) {
        // UPDATE condicional: una vez ejecutada, nadie puede "cancelarla" y
        // dejar el grupo borrado sin solicitud que lo explique.
        repository.cancelarSiPendiente(id, usuarioId, momento);
    }

    @Override
    @Transactional
    public void marcarEjecutada(UUID id, OffsetDateTime momento) {
        // UPDATE condicional + desvinculación de la conversación antes del DELETE.
        repository.ejecutarSiPendiente(id, momento);
    }

    private EliminacionGrupoModel toDomain(EliminacionGrupo entity) {
        return EliminacionGrupoModel.builder()
                .id(entity.getId())
                .conversacionId(entity.getConversacionId())
                .tituloSnapshot(entity.getTituloSnapshot())
                .solicitadoPor(entity.getSolicitadoPor())
                .solicitadoEn(entity.getSolicitadoEn())
                .ejecutarDespues(entity.getEjecutarDespues())
                .estado(entity.getEstado())
                .canceladaPor(entity.getCanceladaPor())
                .canceladaEn(entity.getCanceladaEn())
                .ejecutadaEn(entity.getEjecutadaEn())
                .build();
    }
}
