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
public class ActualizarPosicionGpsRequestDto {
    private Double latitud;
    private Double longitud;
    private Double velocidadKmh;
    private Integer rumboGrados;
}