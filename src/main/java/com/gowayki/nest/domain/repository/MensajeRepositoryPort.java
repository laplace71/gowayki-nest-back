package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.MensajeModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensajeRepositoryPort {
    MensajeModel save(MensajeModel model);
    List<MensajeModel> findByConversacionId(UUID conversacionId);
    Optional<MensajeModel> findUltimoMensaje(UUID conversacionId);
    int contarMensajes(UUID conversacionId);
}

