package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.PuntoControlModel;

import java.util.List;

public interface PuntoControlRepositoryPort {
    List<PuntoControlModel> findByLineaId(Integer lineaId);
}