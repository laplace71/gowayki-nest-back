package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.AlertaIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAlertaIncidenciaRepository extends JpaRepository<AlertaIncidencia, UUID> {
    List<AlertaIncidencia> findByEstado(String estado);
}