package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.EliminacionGrupo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEliminacionGrupoRepository extends JpaRepository<EliminacionGrupo, UUID> {

    Optional<EliminacionGrupo> findByConversacionIdAndEstado(UUID conversacionId, String estado);

    List<EliminacionGrupo> findByEstadoAndEjecutarDespuesBefore(String estado, OffsetDateTime momento);

    List<EliminacionGrupo> findByConversacionIdAndEstadoOrderBySolicitadoEnDesc(UUID conversacionId, String estado);

    /** Bloqueo de fila: cancelar y purgar no pueden correr en paralelo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EliminacionGrupo e WHERE e.id = :id")
    Optional<EliminacionGrupo> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE EliminacionGrupo e SET e.estado = 'CANCELADA', e.canceladaPor = :usuarioId, " +
            "e.canceladaEn = :momento WHERE e.id = :id AND e.estado = 'PENDIENTE'")
    int cancelarSiPendiente(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId,
                            @Param("momento") OffsetDateTime momento);

    @Modifying
    @Query("UPDATE EliminacionGrupo e SET e.estado = 'EJECUTADA', e.ejecutadaEn = :momento, " +
            "e.conversacionId = NULL WHERE e.id = :id AND e.estado = 'PENDIENTE'")
    int ejecutarSiPendiente(@Param("id") UUID id, @Param("momento") OffsetDateTime momento);
}
