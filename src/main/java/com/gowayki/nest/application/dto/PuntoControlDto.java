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
public class PuntoControlDto {
    private Integer id;
    private String nombre;
    private Integer orden;
    private Double latitud;
    private Double longitud;
    private Integer radioToleranciaMetros;
}

