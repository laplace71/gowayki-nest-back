package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataConversacionRepository extends JpaRepository<Conversacion, UUID> {

    @Query("SELECT c FROM Conversacion c JOIN ConversacionParticipante cp ON cp.conversacion.id = c.id " +
            "WHERE cp.chofer.id = :usuarioId " +
            "AND NOT EXISTS (SELECT 1 FROM ConversacionUsuarioEstado e " +
            "                WHERE e.id.conversacionId = c.id AND e.id.usuarioId = :usuarioId AND e.oculta = true) " +
            "ORDER BY c.createdAt DESC")
    List<Conversacion> findByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /**
     * Igual que el anterior pero sin descontar las ocultas: sirve para las
     * operaciones que deben ver toda la membresía real (por ejemplo, al cambiar
     * de subruta hay que salir de los otros grupos aunque estén ocultos).
     */
    @Query("SELECT c FROM Conversacion c JOIN ConversacionParticipante cp ON cp.conversacion.id = c.id " +
            "WHERE cp.chofer.id = :usuarioId " +
            "ORDER BY c.createdAt DESC")
    List<Conversacion> findByUsuarioIdSinFiltroOcultas(@Param("usuarioId") UUID usuarioId);

    java.util.Optional<Conversacion> findFirstByTitulo(String titulo);
}

