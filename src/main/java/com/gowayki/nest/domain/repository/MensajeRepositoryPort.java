package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.MensajeModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MensajeRepositoryPort {
    MensajeModel save(MensajeModel model);

    /** Todos los mensajes, incluidos los ya marcados (lo necesita la purga). */
    List<MensajeModel> findByConversacionId(UUID conversacionId);

    /** Solo los visibles para los usuarios (sin soft delete). */
    List<MensajeModel> findVisiblesByConversacionId(UUID conversacionId);

    /** Visibles para un usuario concreto (respeta su marca de "limpiar"). */
    List<MensajeModel> findVisiblesParaUsuario(UUID conversacionId, UUID usuarioId);

    /** Último mensaje visible para ese usuario (para el resumen del chat). */
    Optional<MensajeModel> findUltimoMensaje(UUID conversacionId, UUID usuarioId);

    /** Cantidad de mensajes visibles para ese usuario. */
    int contarMensajes(UUID conversacionId, UUID usuarioId);

    Optional<MensajeModel> findById(UUID mensajeId);

    /** "Eliminar para mí": marca personal, los demás participantes no la notan. */
    void ocultarParaUsuario(UUID mensajeId, UUID usuarioId);

    /** "Eliminar para todos": soft delete inmediato para todos los participantes. */
    void marcarEliminado(UUID mensajeId, OffsetDateTime momento);

    /** Purga física luego del plazo de 72 horas. */
    void eliminarFisicamente(UUID mensajeId);

    /** Soft delete de los mensajes al eliminar el grupo. */
    void marcarEliminados(UUID conversacionId, OffsetDateTime momento);

    void desmarcarEliminados(UUID conversacionId);

    /**
     * ¿Algún mensaje sigue apuntando a este archivo? Sirve para no borrar un
     * audio que otra conversación (u otro mensaje) todavía usa.
     */
    boolean existeAudioUrl(String audioUrl);
}

