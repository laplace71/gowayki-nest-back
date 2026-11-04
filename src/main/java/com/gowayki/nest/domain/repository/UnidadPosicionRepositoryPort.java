package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.UnidadPosicionModel;

import java.util.Optional;

public interface UnidadPosicionRepositoryPort {
    UnidadPosicionModel save(UnidadPosicionModel posicion);
    Optional<UnidadPosicionModel> findByUnidadId(Integer unidadId);
}