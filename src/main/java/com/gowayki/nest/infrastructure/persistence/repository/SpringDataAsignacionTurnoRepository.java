package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.AsignacionTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAsignacionTurnoRepository extends JpaRepository<AsignacionTurno, UUID> {

    @Query("SELECT t FROM AsignacionTurno t JOIN FETCH t.chofer JOIN FETCH t.unidad u JOIN FETCH u.linea LEFT JOIN FETCH t.subruta WHERE t.chofer.id = :choferId AND t.estado IN ('EN_RUTA', 'PROGRAMADO') ORDER BY t.createdAt DESC LIMIT 1")
    Optional<AsignacionTurno> findTurnoActivoPorChofer(@Param("choferId") UUID choferId);

    @Query("SELECT t FROM AsignacionTurno t JOIN FETCH t.chofer JOIN FETCH t.unidad u JOIN FETCH u.linea LEFT JOIN FETCH t.subruta WHERE t.chofer.id = :choferId ORDER BY t.createdAt DESC")
    List<AsignacionTurno> findByChoferId(@Param("choferId") UUID choferId);

    @Query("SELECT t FROM AsignacionTurno t JOIN FETCH t.chofer JOIN FETCH t.unidad u JOIN FETCH u.linea LEFT JOIN FETCH t.subruta WHERE t.estado = 'EN_RUTA' ORDER BY t.createdAt DESC")
    List<AsignacionTurno> findTurnosActivos();

    /** Escritura atómica: solo cambia si el turno sigue activo/programado. */
    @Modifying
    @Query(value = "UPDATE asignacion_turno SET subruta_id = :subrutaId " +
            "WHERE id = :id AND estado IN ('EN_RUTA', 'PROGRAMADO')", nativeQuery = true)
    int cambiarSubrutaSiActivo(@Param("id") UUID id, @Param("subrutaId") Integer subrutaId);
}
