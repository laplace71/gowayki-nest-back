package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.MensajeLeido;
import com.gowayki.nest.infrastructure.persistence.entity.MensajeLeidoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMensajeLeidoRepository extends JpaRepository<MensajeLeido, MensajeLeidoId> {
}
