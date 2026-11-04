package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.AlertaIncidenciaModel;

import java.util.List;
import java.util.UUID;

public interface AlertaIncidenciaRepositoryPort {
    AlertaIncidenciaModel save(AlertaIncidenciaModel alerta);
    List<AlertaIncidenciaModel> findAll();
    List<AlertaIncidenciaModel> findByEstado(String estado);
}