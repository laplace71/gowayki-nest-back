package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearAlertaSosRequestDto {
    private UUID choferId;
    private Integer unidadId;
    private String tipo;
    private String origenDisparo;
    private Double latitud;
    private Double longitud;
    private String descripcion;
    private String audioUrl;
}