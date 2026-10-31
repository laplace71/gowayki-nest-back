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
public class UnidadModel {
    private Integer id;
    private String numeroPadron;
    private String placa;
    private Integer lineaId;
    private String lineaNombre;
    private String estado;
    private PosicionGpsModel posicionActual;
}

