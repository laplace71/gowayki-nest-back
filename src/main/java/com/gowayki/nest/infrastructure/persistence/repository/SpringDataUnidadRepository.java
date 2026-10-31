package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataUnidadRepository extends JpaRepository<Unidad, Integer> {

    @Query("SELECT u FROM Unidad u JOIN FETCH u.linea WHERE u.linea.id = :lineaId")
    List<Unidad> findByLineaId(@Param("lineaId") Integer lineaId);

    @Query("SELECT u FROM Unidad u JOIN FETCH u.linea")
    List<Unidad> findAllWithLinea();
}

