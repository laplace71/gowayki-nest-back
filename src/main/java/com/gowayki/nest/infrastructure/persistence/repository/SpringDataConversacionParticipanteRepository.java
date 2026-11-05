package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipante;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipanteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataConversacionParticipanteRepository extends JpaRepository<ConversacionParticipante, ConversacionParticipanteId> {
    boolean existsByIdConversacionIdAndIdChoferId(UUID conversacionId, UUID choferId);
}

