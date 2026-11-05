package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarTurnoRequestDto {
    private UUID choferId;
    private Integer unidadId;
    private LocalDate fecha;
    private OffsetDateTime horaInicio;
    private OffsetDateTime horaFin;
}
