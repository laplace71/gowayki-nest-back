package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineaResponseDto {
    private Integer id;
    private String nombre;
    private String origen;
    private String destino;
    private String corredor;
    private String colorHex;
    @Builder.Default
    private List<PuntoControlDto> paraderos = new ArrayList<>();
}

