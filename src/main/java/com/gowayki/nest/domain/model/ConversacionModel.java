package com.gowayki.nest.domain.model;

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
public class ConversacionModel {
    private UUID id;
    private String titulo;
    private Boolean esGrupo;
    /** PRIVADA, GRUPO o GRUPO_RUTA. */
    private String tipoConversacion;
    /** Subruta concreta cuando el tipo es GRUPO_RUTA. */
    private String claveRuta;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    /** Soft delete de la eliminación definitiva del grupo (72 h). */
    private OffsetDateTime eliminadoEn;
}

