package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearConversacionRequestDto {
    private String titulo;
    private Boolean esGrupo;
    /** PRIVADA, GRUPO o GRUPO_RUTA. Opcional: si falta se deduce del título. */
    private String tipoConversacion;
    /** Subruta concreta para un GRUPO_RUTA. */
    private String claveRuta;
    private UUID createdBy;
    private List<UUID> participanteIds;
}

