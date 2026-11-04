package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataConversacionRepository extends JpaRepository<Conversacion, UUID> {

    @Query("SELECT c FROM Conversacion c JOIN ConversacionParticipante cp ON cp.conversacion.id = c.id WHERE cp.chofer.id = :usuarioId ORDER BY c.createdAt DESC")
    List<Conversacion> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
