package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipante;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipanteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringDataConversacionParticipanteRepository extends JpaRepository<ConversacionParticipante, ConversacionParticipanteId> {
    boolean existsByIdConversacionIdAndIdChoferId(UUID conversacionId, UUID choferId);
    long countByIdConversacionId(UUID conversacionId);
    java.util.List<ConversacionParticipante> findAllByIdConversacionId(UUID conversacionId);

    @Modifying
    @Query("DELETE FROM ConversacionParticipante cp WHERE cp.id.conversacionId = :conversacionId")
    void deleteAllByIdConversacionId(@Param("conversacionId") UUID conversacionId);
}

