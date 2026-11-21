package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.MensajeOculto;
import com.gowayki.nest.infrastructure.persistence.entity.MensajeOcultoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMensajeOcultoRepository extends JpaRepository<MensajeOculto, MensajeOcultoId> {
}
