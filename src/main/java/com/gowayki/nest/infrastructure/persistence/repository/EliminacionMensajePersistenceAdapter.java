package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.EliminacionMensajeModel;
import com.gowayki.nest.domain.repository.EliminacionMensajePort;
import com.gowayki.nest.infrastructure.persistence.entity.EliminacionMensaje;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EliminacionMensajePersistenceAdapter implements EliminacionMensajePort {

    private final SpringDataEliminacionMensajeRepository repository;

    public EliminacionMensajePersistenceAdapter(SpringDataEliminacionMensajeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EliminacionMensajeModel> buscarPorId(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<EliminacionMensajeModel> buscarPendiente(UUID mensajeId) {
        return repository.findByMensajeIdAndEstado(mensajeId, EliminacionMensaje.PENDIENTE)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public EliminacionMensajeModel guardar(EliminacionMensajeModel model) {
        EliminacionMensaje entity = EliminacionMensaje.builder()
                .id(model.getId())
                .mensajeId(model.getMensajeId())
                .conversacionId(model.getConversacionId())
                .solicitadoPor(model.getSolicitadoPor())
                .solicitadoEn(model.getSolicitadoEn() == null ? OffsetDateTime.now() : model.getSolicitadoEn())
                .ejecutarDespues(model.getEjecutarDespues())
                .estado(model.getEstado() == null ? EliminacionMensaje.PENDIENTE : model.getEstado())
                .ejecutadaEn(model.getEjecutadaEn())
                .build();
        return toDomain(repository.save(entity));
    }

    @Override
    public List<EliminacionMensajeModel> pendientesVencidas(OffsetDateTime ahora) {
        return repository
                .findByEstadoAndEjecutarDespuesBefore(EliminacionMensaje.PENDIENTE, ahora)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void marcarEjecutada(UUID id, OffsetDateTime momento) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEstado(EliminacionMensaje.EJECUTADA);
            entity.setEjecutadaEn(momento);
            repository.save(entity);
        });
    }

    private EliminacionMensajeModel toDomain(EliminacionMensaje entity) {
        return EliminacionMensajeModel.builder()
                .id(entity.getId())
                .mensajeId(entity.getMensajeId())
                .conversacionId(entity.getConversacionId())
                .solicitadoPor(entity.getSolicitadoPor())
                .solicitadoEn(entity.getSolicitadoEn())
                .ejecutarDespues(entity.getEjecutarDespues())
                .estado(entity.getEstado())
                .ejecutadaEn(entity.getEjecutadaEn())
                .build();
    }
}
