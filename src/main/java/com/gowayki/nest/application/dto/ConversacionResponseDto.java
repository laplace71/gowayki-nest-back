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
}

