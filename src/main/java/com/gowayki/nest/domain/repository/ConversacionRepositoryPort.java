package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.ConversacionModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversacionRepositoryPort {
    ConversacionModel save(ConversacionModel model, List<UUID> participanteIds);
    Optional<ConversacionModel> findById(UUID id);
    List<ConversacionModel> findByUsuarioId(UUID usuarioId);

    /** Igual que `findByUsuarioId` pero incluyendo las que el usuario ocultó. */
    List<ConversacionModel> findByUsuarioIdSinFiltroOcultas(UUID usuarioId);
    boolean existeParticipante(UUID conversacionId, UUID usuarioId);
    Optional<ConversacionModel> findByTitulo(String titulo);

    /** Grupo de subruta por su clave estable (nombre de la subruta). */
    Optional<ConversacionModel> findByClaveRuta(String claveRuta);
    void agregarParticipante(UUID conversacionId, UUID usuarioId);
    int contarParticipantes(UUID conversacionId);
    boolean esAdministrador(UUID conversacionId, UUID usuarioId);

    /** Rol del usuario dentro de la conversación: ADMIN, MIEMBRO o null si no participa. */
    String obtenerRol(UUID conversacionId, UUID usuarioId);

    /** Saca al usuario de la conversación (lo que en la app es "Salir del grupo"). */
    void quitarParticipante(UUID conversacionId, UUID usuarioId);

    /** Cambia el rol de un participante dentro de la conversación (ADMIN o MIEMBRO). */
    void cambiarRol(UUID conversacionId, UUID usuarioId, String nuevoRol);

    /** Transfiere la creación de la conversación a otro participante activo. */
    void cambiarCreador(UUID conversacionId, UUID nuevoCreadorId);

    /** Lista los participantes de la conversación (id, nombre, rol). */
    java.util.List<com.gowayki.nest.domain.model.ParticipanteConversacion> listarParticipantes(UUID conversacionId);

    /**
     * Borra la conversación y, por las FK ON DELETE CASCADE de la base, sus
     * participantes y mensajes.
     */
    void eliminar(UUID conversacionId);

    /** Soft delete: marca el grupo como eliminado sin borrar nada todavía. */
    void marcarEliminado(UUID conversacionId, OffsetDateTime momento);

    /** Quita la marca si se cancela la eliminación dentro de las 72 horas. */
    void desmarcarEliminado(UUID conversacionId);

    /** Oculta la conversación solo para ese usuario. No borra nada. */
    void ocultarParaUsuario(UUID conversacionId, UUID usuarioId);

    /** Vuelve a mostrar una conversación que el usuario había ocultado. */
    void mostrarParaUsuario(UUID conversacionId, UUID usuarioId);

    /**
     * Guarda la marca de "limpiar chat" de este usuario. No toca mensajes ni
     * avisa a nadie: es solo visual y personal.
     */
    void limpiarParaUsuario(UUID conversacionId, UUID usuarioId, OffsetDateTime momento);

    /** Borra el estado personal (al salir del grupo, para que al volver se vea). */
    void limpiarEstadoUsuario(UUID conversacionId, UUID usuarioId);

    /**
     * Bloquea la conversación para mutar su membresía con seguridad
     * (`SELECT ... FOR UPDATE`). Debe llamarse dentro de una transacción.
     */
    Optional<ConversacionModel> bloquearParaActualizar(UUID conversacionId);

    /**
     * Si el grupo no tiene ningún administrador, asciende al participante más
     * antiguo (desempate por UUID) y devuelve su id. Si ya hay administrador o
     * el grupo está vacío, devuelve vacío.
     */
    Optional<UUID> promoverAdministradorSiHaceFalta(UUID conversacionId);
}

