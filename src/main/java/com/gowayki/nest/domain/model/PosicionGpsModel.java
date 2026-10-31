package com.gowayki.nest.domain.model;

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
public class PosicionGpsModel {
    private Double latitud;
    private Double longitud;
    private Double velocidadKmh;
    private Integer rumboGrados;
    private String ultimaActualizacion;
}

