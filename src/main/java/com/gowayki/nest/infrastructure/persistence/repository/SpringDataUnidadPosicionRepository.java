package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.UnidadPosicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUnidadPosicionRepository extends JpaRepository<UnidadPosicion, Integer> {
}

