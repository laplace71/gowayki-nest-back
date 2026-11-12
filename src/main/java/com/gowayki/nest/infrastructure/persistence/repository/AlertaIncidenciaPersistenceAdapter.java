package com.gowayki.nest.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gowayki.nest.domain.model.AlertaIncidenciaModel;
import com.gowayki.nest.domain.repository.AlertaIncidenciaRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.AlertaIncidencia;
import com.gowayki.nest.infrastructure.persistence.mapper.AlertaIncidenciaMapper;

@Component
public class AlertaIncidenciaPersistenceAdapter implements AlertaIncidenciaRepositoryPort {

    private final SpringDataAlertaIncidenciaRepository springDataAlertaIncidenciaRepository;
    private final AlertaIncidenciaMapper alertaIncidenciaMapper;
    private final SpringDataUsuarioRepository springDataUsuarioRepository;
    private final SpringDataUnidadRepository springDataUnidadRepository;

    public AlertaIncidenciaPersistenceAdapter(SpringDataAlertaIncidenciaRepository springDataAlertaIncidenciaRepository,
                                             AlertaIncidenciaMapper alertaIncidenciaMapper,
                                             SpringDataUsuarioRepository springDataUsuarioRepository,
                                             SpringDataUnidadRepository springDataUnidadRepository) {
        this.springDataAlertaIncidenciaRepository = springDataAlertaIncidenciaRepository;
        this.alertaIncidenciaMapper = alertaIncidenciaMapper;
        this.springDataUsuarioRepository = springDataUsuarioRepository;
        this.springDataUnidadRepository = springDataUnidadRepository;
    }

    @Override
    public AlertaIncidenciaModel save(AlertaIncidenciaModel model) {
        AlertaIncidencia entity = alertaIncidenciaMapper.toEntity(model);
        if (model.getChoferId() != null) {
            springDataUsuarioRepository.findById(model.getChoferId()).ifPresent(entity::setChofer);
        }
        if (model.getUnidadId() != null) {
            springDataUnidadRepository.findById(model.getUnidadId()).ifPresent(entity::setUnidad);
        }
        AlertaIncidencia guardado = springDataAlertaIncidenciaRepository.save(entity);
        return alertaIncidenciaMapper.toDomain(guardado);
    }

    @Override
    public List<AlertaIncidenciaModel> findAll() {
        return springDataAlertaIncidenciaRepository.findAll().stream()
                .map(alertaIncidenciaMapper::toDomain)
                .toList();
    }

    @Override
    public List<AlertaIncidenciaModel> findByEstado(String estado) {
        return springDataAlertaIncidenciaRepository.findByEstado(estado).stream()
                .map(alertaIncidenciaMapper::toDomain)
                .toList();
    }
}