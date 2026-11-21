package com.gowayki.nest.application.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoRutasDto {
    private List<LineaCatalogoDto> lineas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineaCatalogoDto {
        private String codigo;
        private String nombre;
        private String colorHex;
        private List<SubrutaDto> subrutas;
    }
}
