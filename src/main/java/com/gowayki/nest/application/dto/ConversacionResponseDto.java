package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionResponseDto {
    private UUID id;
    private String titulo;
    private Boolean esGrupo;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private Integer totalMensajes;
    private MensajeResponseDto ultimoMensaje;
    private Integer totalParticipantes;
    /** Rol del usuario que pidió la lista dentro de esta conversación (ADMIN, MIEMBRO o null). */
    private String miRol;
    /** true si hay una solicitud de eliminación definitiva esperando las 72 h. */
    private Boolean eliminacionPendiente;
    /** Cuándo se ejecutará la eliminación definitiva, si está pendiente. */
    private OffsetDateTime eliminacionProgramadaPara;
}

