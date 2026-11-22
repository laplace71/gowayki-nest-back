package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Subruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSubrutaRepository extends JpaRepository<Subruta, Integer> {
    List<Subruta> findByActivaTrueOrderByLineaCodigoAscNombreAsc();

    /** Catálogo sin el BLOB de imagen. */
    @Query("SELECT s.id AS id, s.lineaCodigo AS lineaCodigo, s.lineaNombre AS lineaNombre, " +
            "s.codigo AS codigo, s.nombre AS nombre, s.operador AS operador, " +
            "s.colorHex AS colorHex, s.routeIdIda AS routeIdIda, " +
            "s.routeIdRetorno AS routeIdRetorno, s.nombreRetorno AS nombreRetorno, " +
            "s.activa AS activa FROM Subruta s WHERE s.activa = true " +
            "ORDER BY s.lineaCodigo ASC, s.nombre ASC")
    List<SubrutaCatalogoProjection> findCatalogo();
    List<Subruta> findByLineaCodigoOrderByNombreAsc(String lineaCodigo);
    Optional<Subruta> findByCodigo(String codigo);
    Optional<Subruta> findByRouteIdIda(String routeIdIda);
    Optional<Subruta> findByRouteIdRetorno(String routeIdRetorno);
}
