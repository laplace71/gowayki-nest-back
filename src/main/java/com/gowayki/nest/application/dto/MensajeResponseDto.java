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
public class MensajeResponseDto {
    private UUID id;
    private UUID conversacionId;
    private UUID emisorId;
    private String emisorNombre;
    private String tipo;
    private String contenidoTexto;
    private String audioUrl;
    private Integer audioDuracionSegundos;
    private String estado;
    private OffsetDateTime createdAt;
}
