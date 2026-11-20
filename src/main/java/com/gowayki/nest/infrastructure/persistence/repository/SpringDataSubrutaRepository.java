package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Subruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSubrutaRepository extends JpaRepository<Subruta, Integer> {
    List<Subruta> findByActivaTrueOrderByLineaCodigoAscNombreAsc();
    List<Subruta> findByLineaCodigoOrderByNombreAsc(String lineaCodigo);
    Optional<Subruta> findByCodigo(String codigo);
    Optional<Subruta> findByRouteIdIda(String routeIdIda);
    Optional<Subruta> findByRouteIdRetorno(String routeIdRetorno);
}
