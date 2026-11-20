package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.EliminacionMensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEliminacionMensajeRepository extends JpaRepository<EliminacionMensaje, UUID> {

    Optional<EliminacionMensaje> findByMensajeIdAndEstado(UUID mensajeId, String estado);

    List<EliminacionMensaje> findByEstadoAndEjecutarDespuesBefore(String estado, OffsetDateTime momento);
}
