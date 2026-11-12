package com.gowayki.nest.domain.repository;

import java.util.List;

import com.gowayki.nest.domain.model.AlertaIncidenciaModel;

public interface AlertaIncidenciaRepositoryPort {
    AlertaIncidenciaModel save(AlertaIncidenciaModel alerta);
    List<AlertaIncidenciaModel> findAll();
    List<AlertaIncidenciaModel> findByEstado(String estado);
}