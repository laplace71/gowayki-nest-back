package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataConversacionRepository extends JpaRepository<Conversacion, UUID> {

    @Query("SELECT c FROM Conversacion c JOIN ConversacionParticipante cp ON cp.conversacion.id = c.id " +
            "WHERE cp.chofer.id = :usuarioId AND c.eliminadoEn IS NULL " +
            "AND NOT EXISTS (SELECT 1 FROM ConversacionUsuarioEstado e " +
            "                WHERE e.id.conversacionId = c.id AND e.id.usuarioId = :usuarioId AND e.oculta = true) " +
            "AND NOT EXISTS (SELECT 1 FROM ConversacionUsuarioEstado l " +
            "                WHERE l.id.conversacionId = c.id AND l.id.usuarioId = :usuarioId " +
            "                  AND l.limpiadoEn IS NOT NULL " +
            "                  AND NOT EXISTS (SELECT 1 FROM Mensaje m2 " +
            "                                  WHERE m2.conversacion.id = c.id AND m2.eliminadoEn IS NULL " +
            "                                    AND m2.createdAt > l.limpiadoEn)) " +
            "ORDER BY c.createdAt DESC")
    List<Conversacion> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Igual que el anterior pero sin descontar las ocultas: sirve para las
     * operaciones que deben ver toda la membresía real (por ejemplo, al cambiar
     * de subruta hay que salir de los otros grupos aunque estén ocultos).
     */
    @Query("SELECT c FROM Conversacion c JOIN ConversacionParticipante cp ON cp.conversacion.id = c.id " +
            "WHERE cp.chofer.id = :usuarioId AND c.eliminadoEn IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Conversacion> findByUsuarioIdSinFiltroOcultas(@Param("usuarioId") UUID usuarioId);

    /**
     * Igual que la anterior pero INCLUYE grupos con purga pendiente: al borrar
     * una cuenta hay que cancelar la solicitud si el creador se va, en vez de
     * dejar que el programador borre el grupo para los demás.
     */
    @Query("SELECT c FROM Conversacion c JOIN ConversacionParticipante cp ON cp.conversacion.id = c.id " +
            "WHERE cp.chofer.id = :usuarioId ORDER BY c.createdAt DESC")
    List<Conversacion> findByUsuarioIdIncluyendoPendientes(@Param("usuarioId") UUID usuarioId);

    /** Marca de soft delete al pedir la eliminación definitiva. */
    @Modifying
    @Query("UPDATE Conversacion c SET c.eliminadoEn = :momento WHERE c.id = :conversacionId")
    void marcarEliminado(@Param("conversacionId") UUID conversacionId,
                         @Param("momento") OffsetDateTime momento);

    /** Quita la marca si se cancela la eliminación dentro de las 72 horas. */
    @Modifying
    @Query("UPDATE Conversacion c SET c.eliminadoEn = NULL WHERE c.id = :conversacionId")
    void desmarcarEliminado(@Param("conversacionId") UUID conversacionId);

    @Modifying
    @Query(value = "UPDATE conversacion SET created_by = :nuevo WHERE id = :id", nativeQuery = true)
    void cambiarCreador(@Param("id") UUID id, @Param("nuevo") UUID nuevo);

    java.util.Optional<Conversacion> findFirstByTitulo(String titulo);

    java.util.Optional<Conversacion> findFirstByClaveRuta(String claveRuta);

    /**
     * Bloquea la fila de la conversación hasta el final de la transacción.
     * Se usa antes de tocar la membresía para que dos peticiones simultáneas no
     * se pisen (por ejemplo, dos "salir" que dejan el grupo sin nadie).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Conversacion c WHERE c.id = :id")
    java.util.Optional<Conversacion> findByIdForUpdate(@Param("id") UUID id);
}

