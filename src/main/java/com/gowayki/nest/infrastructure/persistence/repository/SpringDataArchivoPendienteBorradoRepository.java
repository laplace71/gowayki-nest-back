package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.ArchivoPendienteBorrado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataArchivoPendienteBorradoRepository
        extends JpaRepository<ArchivoPendienteBorrado, UUID> {

    boolean existsByRuta(String ruta);

    Optional<ArchivoPendienteBorrado> findByRuta(String ruta);

    List<ArchivoPendienteBorrado> findAllByOrderByCreadoEnAsc(Pageable pageable);

    /**
     * Pendientes que ya toca reintentar: nunca intentados o con el último
     * intento anterior al momento indicado. Así una ruta que falla no bloquea
     * a las demás.
     */
    @Query("SELECT a FROM ArchivoPendienteBorrado a " +
            "WHERE a.ultimoIntentoEn IS NULL OR a.ultimoIntentoEn < :antes " +
            "ORDER BY a.creadoEn ASC")
    List<ArchivoPendienteBorrado> findParaIntentar(@Param("antes") OffsetDateTime antes, Pageable pageable);
}
