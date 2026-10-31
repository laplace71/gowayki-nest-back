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
public class PuntoControlModel {
    private Integer id;
    private String nombre;
    private Integer orden;
    private Double latitud;
    private Double longitud;
    private Integer radioToleranciaMetros;
    private Integer toleranciaMinutos;
}

