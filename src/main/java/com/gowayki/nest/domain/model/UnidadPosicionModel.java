package com.gowayki.nest.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadPosicionModel {
    private Integer unidadId;
    private Double latitud;
    private Double longitud;
    private Double velocidadKmh;
    private Integer rumboGrados;
    private OffsetDateTime updatedAt;
}