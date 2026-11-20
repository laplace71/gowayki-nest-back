package com.gowayki.nest.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubrutaDto {
    private Integer id;
    private String lineaCodigo;
    private String lineaNombre;
    private String codigo;
    private String nombre;
    private String operador;
    private String colorHex;
    private String routeIdIda;
    private String routeIdRetorno;
    private String nombreRetorno;
    private Boolean activa;
}
