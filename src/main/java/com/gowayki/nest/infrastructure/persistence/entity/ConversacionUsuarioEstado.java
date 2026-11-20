package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Estado de una conversación para un usuario concreto. Hoy solo se usa para
 * saber si la ocultó de su lista; no borra nada de la conversación.
 */
@Entity
@Table(name = "conversacion_usuario_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionUsuarioEstado {

    @EmbeddedId
    private ConversacionUsuarioEstadoId id;

    @Column(nullable = false)
    @Builder.Default
    private Boolean oculta = false;

    @Column(name = "oculta_en")
    private OffsetDateTime ocultaEn;

    /** Marca de "limpiar chat": solo este usuario oculta lo anterior. */
    @Column(name = "limpiado_en")
    private OffsetDateTime limpiadoEn;
}
