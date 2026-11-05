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
    private UUID createdBy;
    private List<UUID> participanteIds;
}

