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
    boolean esAdministrador(UUID conversacionId, UUID usuarioId);

    /** Rol del usuario dentro de la conversación: ADMIN, MIEMBRO o null si no participa. */
    String obtenerRol(UUID conversacionId, UUID usuarioId);

    /** Saca al usuario de la conversación (lo que en la app es "Salir del grupo"). */
    void quitarParticipante(UUID conversacionId, UUID usuarioId);

    /** Cambia el rol de un participante dentro de la conversación (ADMIN o MIEMBRO). */
    void cambiarRol(UUID conversacionId, UUID usuarioId, String nuevoRol);

    /**
     * Borra la conversación y, por las FK ON DELETE CASCADE de la base, sus
     * participantes y mensajes.
     */
    void eliminar(UUID conversacionId);
}

