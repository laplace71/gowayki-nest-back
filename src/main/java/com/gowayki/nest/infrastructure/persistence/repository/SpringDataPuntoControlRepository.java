package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.PuntoControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataPuntoControlRepository extends JpaRepository<PuntoControl, Integer> {
    List<PuntoControl> findByLineaIdOrderByOrdenAsc(Integer lineaId);
}

