package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.LineaModel;

import java.util.List;
import java.util.Optional;

public interface LineaRepositoryPort {
    List<LineaModel> findAll();
    Optional<LineaModel> findById(Integer id);
}

