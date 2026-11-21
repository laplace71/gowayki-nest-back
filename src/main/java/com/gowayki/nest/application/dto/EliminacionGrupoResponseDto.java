package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Respuesta al pedir la eliminación definitiva de un grupo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EliminacionGrupoResponseDto {
    private UUID conversacionId;
    private UUID solicitudId;
    private String estado;
    private OffsetDateTime solicitadoEn;
    /** Momento a partir del cual la purga borrará el grupo. */
    private OffsetDateTime seEliminaraEn;
    private Integer horasDeEspera;
}
