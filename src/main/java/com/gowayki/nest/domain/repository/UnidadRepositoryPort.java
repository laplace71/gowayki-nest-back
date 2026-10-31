package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.UnidadModel;

import java.util.List;
import java.util.Optional;

public interface UnidadRepositoryPort {
    List<UnidadModel> findAll();
    List<UnidadModel> findByLineaId(Integer lineaId);
    Optional<UnidadModel> findById(Integer id);
}

