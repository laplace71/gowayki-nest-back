package com.gowayki.nest.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParaderoRutaDto {
    private String id;
    private String name;
    private Double lat;
    private Double lon;
    private String district;
    private Integer orden;
}
