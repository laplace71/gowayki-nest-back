package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMensajeRepository extends JpaRepository<Mensaje, UUID> {

    @Query("SELECT m FROM Mensaje m JOIN FETCH m.emisor WHERE m.conversacion.id = :conversacionId ORDER BY m.createdAt ASC")
    List<Mensaje> findByConversacionIdOrderByCreatedAtAsc(@Param("conversacionId") UUID conversacionId);

    @Query("SELECT m FROM Mensaje m JOIN FETCH m.emisor WHERE m.conversacion.id = :conversacionId ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Mensaje> findTopByConversacionIdOrderByCreatedAtDesc(@Param("conversacionId") UUID conversacionId);

    int countByConversacionId(UUID conversacionId);
}

