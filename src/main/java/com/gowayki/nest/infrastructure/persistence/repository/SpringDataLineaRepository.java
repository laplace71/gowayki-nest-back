package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Linea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLineaRepository extends JpaRepository<Linea, Integer> {
}

