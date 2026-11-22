package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMensajeRepository extends JpaRepository<Mensaje, UUID> {

    @Query("SELECT m FROM Mensaje m JOIN FETCH m.emisor WHERE m.conversacion.id = :conversacionId ORDER BY m.createdAt ASC")
    List<Mensaje> findByConversacionIdOrderByCreatedAtAsc(@Param("conversacionId") UUID conversacionId);

    /** Los que la app debe mostrar (excluye los marcados como eliminados). */
    @Query("SELECT m FROM Mensaje m JOIN FETCH m.emisor WHERE m.conversacion.id = :conversacionId " +
            "AND m.eliminadoEn IS NULL ORDER BY m.createdAt ASC")
    List<Mensaje> findVisiblesByConversacionIdOrderByCreatedAtAsc(@Param("conversacionId") UUID conversacionId);

    /**
     * Visibles para ESTE usuario: oculta lo anterior a su marca de "limpiar" y
     * los mensajes que él mismo marcó como ocultos para sí.
     * El historial completo se muestra salvo que la cuenta se haya reactivado
     * tras una eliminación: en ese caso solo se ven mensajes posteriores al
     * reingreso.
     */
    @Query("SELECT m FROM Mensaje m JOIN FETCH m.emisor WHERE m.conversacion.id = :conversacionId " +
            "AND m.eliminadoEn IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM ConversacionUsuarioEstado e " +
            "                WHERE e.id.conversacionId = :conversacionId AND e.id.usuarioId = :usuarioId " +
            "                  AND e.limpiadoEn IS NOT NULL AND m.createdAt <= e.limpiadoEn) " +
            "AND NOT EXISTS (SELECT 1 FROM MensajeOculto mo " +
            "                WHERE mo.id.mensajeId = m.id AND mo.id.usuarioId = :usuarioId) " +
            "AND ((SELECT u.reactivadoEn FROM Usuario u WHERE u.id = :usuarioId) IS NULL " +
            "     OR m.createdAt >= COALESCE((SELECT cp.agregadoEn FROM ConversacionParticipante cp " +
            "                WHERE cp.id.conversacionId = :conversacionId AND cp.id.choferId = :usuarioId), m.createdAt)) " +
            "ORDER BY m.createdAt ASC")
    List<Mensaje> findVisiblesParaUsuario(@Param("conversacionId") UUID conversacionId,
                                          @Param("usuarioId") UUID usuarioId);

    @Query("SELECT m FROM Mensaje m JOIN FETCH m.emisor WHERE m.conversacion.id = :conversacionId " +
            "AND m.eliminadoEn IS NULL ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Mensaje> findTopByConversacionIdOrderByCreatedAtDesc(@Param("conversacionId") UUID conversacionId);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.conversacion.id = :conversacionId AND m.eliminadoEn IS NULL")
    int countByConversacionId(@Param("conversacionId") UUID conversacionId);

    /** Último mensaje visible para ESTE usuario: respeta "limpiar" y "ocultar para mí". */
    @Query("SELECT m FROM Mensaje m JOIN FETCH m.emisor WHERE m.conversacion.id = :conversacionId " +
            "AND m.eliminadoEn IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM ConversacionUsuarioEstado e " +
            "                WHERE e.id.conversacionId = :conversacionId AND e.id.usuarioId = :usuarioId " +
            "                  AND e.limpiadoEn IS NOT NULL AND m.createdAt <= e.limpiadoEn) " +
            "AND NOT EXISTS (SELECT 1 FROM MensajeOculto mo " +
            "                WHERE mo.id.mensajeId = m.id AND mo.id.usuarioId = :usuarioId) " +
            "AND ((SELECT u.reactivadoEn FROM Usuario u WHERE u.id = :usuarioId) IS NULL " +
            "     OR m.createdAt >= COALESCE((SELECT cp.agregadoEn FROM ConversacionParticipante cp " +
            "                WHERE cp.id.conversacionId = :conversacionId AND cp.id.choferId = :usuarioId), m.createdAt)) " +
            "ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Mensaje> findTopVisibleParaUsuario(@Param("conversacionId") UUID conversacionId,
                                                @Param("usuarioId") UUID usuarioId);

    /** Cantidad visible para ESTE usuario (mismo filtro que el historial). */
    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.conversacion.id = :conversacionId " +
            "AND m.eliminadoEn IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM ConversacionUsuarioEstado e " +
            "                WHERE e.id.conversacionId = :conversacionId AND e.id.usuarioId = :usuarioId " +
            "                  AND e.limpiadoEn IS NOT NULL AND m.createdAt <= e.limpiadoEn) " +
            "AND NOT EXISTS (SELECT 1 FROM MensajeOculto mo " +
            "                WHERE mo.id.mensajeId = m.id AND mo.id.usuarioId = :usuarioId) " +
            "AND ((SELECT u.reactivadoEn FROM Usuario u WHERE u.id = :usuarioId) IS NULL " +
            "     OR m.createdAt >= COALESCE((SELECT cp.agregadoEn FROM ConversacionParticipante cp " +
            "                WHERE cp.id.conversacionId = :conversacionId AND cp.id.choferId = :usuarioId), m.createdAt))")
    int countVisiblesParaUsuario(@Param("conversacionId") UUID conversacionId,
                                 @Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.conversacion.id = :conversacionId " +
            "AND m.eliminadoEn IS NULL AND m.emisor.id <> :usuarioId " +
            "AND NOT EXISTS (SELECT 1 FROM MensajeLeido ml " +
            "                WHERE ml.id.mensajeId = m.id AND ml.id.usuarioId = :usuarioId) " +
            "AND NOT EXISTS (SELECT 1 FROM ConversacionUsuarioEstado e " +
            "                WHERE e.id.conversacionId = :conversacionId AND e.id.usuarioId = :usuarioId " +
            "                  AND e.limpiadoEn IS NOT NULL AND m.createdAt <= e.limpiadoEn) " +
            "AND NOT EXISTS (SELECT 1 FROM MensajeOculto mo " +
            "                WHERE mo.id.mensajeId = m.id AND mo.id.usuarioId = :usuarioId) " +
            "AND ((SELECT u.reactivadoEn FROM Usuario u WHERE u.id = :usuarioId) IS NULL " +
            "     OR m.createdAt >= COALESCE((SELECT cp.agregadoEn FROM ConversacionParticipante cp " +
            "                WHERE cp.id.conversacionId = :conversacionId AND cp.id.choferId = :usuarioId), m.createdAt))")
    int contarNoLeidos(@Param("conversacionId") UUID conversacionId,
                       @Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query("UPDATE Mensaje m SET m.estado = 'LEIDO' WHERE m.conversacion.id = :conversacionId " +
            "AND m.emisor.id <> :usuarioId")
    void marcarLeidos(@Param("conversacionId") UUID conversacionId,
                      @Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query("UPDATE Mensaje m SET m.estado = 'LEIDO' WHERE m.conversacion.id = :conversacionId " +
            "AND m.emisor.id <> :usuarioId")
    void marcarLeidosParaUsuario(@Param("conversacionId") UUID conversacionId,
                                 @Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query("DELETE FROM Mensaje m WHERE m.conversacion.id = :conversacionId")
    void deleteByConversacionId(@Param("conversacionId") UUID conversacionId);

    @Modifying
    @Query("UPDATE Mensaje m SET m.eliminadoEn = :momento WHERE m.id = :mensajeId")
    void marcarEliminado(@Param("mensajeId") UUID mensajeId, @Param("momento") OffsetDateTime momento);

    @Modifying
    @Query("DELETE FROM Mensaje m WHERE m.id = :mensajeId")
    void eliminarFisicamente(@Param("mensajeId") UUID mensajeId);

    @Modifying
    @Query("UPDATE Mensaje m SET m.eliminadoEn = :momento " +
            "WHERE m.conversacion.id = :conversacionId AND m.eliminadoEn IS NULL")
    void marcarEliminados(@Param("conversacionId") UUID conversacionId,
                          @Param("momento") OffsetDateTime momento);

    @Modifying
    @Query("UPDATE Mensaje m SET m.eliminadoEn = NULL " +
            "WHERE m.conversacion.id = :conversacionId")
    void desmarcarEliminados(@Param("conversacionId") UUID conversacionId);

    boolean existsByAudioUrl(String audioUrl);
}
