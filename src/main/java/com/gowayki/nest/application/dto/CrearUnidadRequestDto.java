package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearUnidadRequestDto {
    private String numeroPadron;
    private String placa;
    private Integer lineaId;
    private Integer subrutaId;
}
