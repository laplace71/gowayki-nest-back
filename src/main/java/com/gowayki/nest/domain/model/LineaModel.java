package com.gowayki.nest.domain.model;

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
public class LineaModel {
    private Integer id;
    private String nombre;
    private String origen;
    private String destino;
    private String corredor;
    private String colorHex;
    @Builder.Default
    private List<PuntoControlModel> puntosControl = new ArrayList<>();
}

