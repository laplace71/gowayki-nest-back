package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.ConversacionModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversacionRepositoryPort {
    ConversacionModel save(ConversacionModel model, List<UUID> participanteIds);
    Optional<ConversacionModel> findById(UUID id);
    List<ConversacionModel> findByUsuarioId(UUID usuarioId);
    boolean existeParticipante(UUID conversacionId, UUID usuarioId);
    Optional<ConversacionModel> findByTitulo(String titulo);
    void agregarParticipante(UUID conversacionId, UUID usuarioId);
    int contarParticipantes(UUID conversacionId);
}

