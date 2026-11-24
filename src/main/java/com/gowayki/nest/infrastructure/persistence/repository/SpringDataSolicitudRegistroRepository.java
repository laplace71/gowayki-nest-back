package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.SolicitudRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataSolicitudRegistroRepository extends JpaRepository<SolicitudRegistro, Long> {
}
