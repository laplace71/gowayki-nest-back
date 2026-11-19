package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.EliminacionGrupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEliminacionGrupoRepository extends JpaRepository<EliminacionGrupo, UUID> {

    Optional<EliminacionGrupo> findByConversacionIdAndEstado(UUID conversacionId, String estado);

    List<EliminacionGrupo> findByEstadoAndEjecutarDespuesBefore(String estado, OffsetDateTime momento);

    List<EliminacionGrupo> findByConversacionIdAndEstadoOrderBySolicitadoEnDesc(UUID conversacionId, String estado);
}
