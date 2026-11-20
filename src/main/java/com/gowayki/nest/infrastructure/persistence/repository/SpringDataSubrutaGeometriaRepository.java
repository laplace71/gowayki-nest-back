package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.SubrutaGeometria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataSubrutaGeometriaRepository extends JpaRepository<SubrutaGeometria, Long> {
    Optional<SubrutaGeometria> findBySubrutaIdAndSentido(Integer subrutaId, String sentido);
}
