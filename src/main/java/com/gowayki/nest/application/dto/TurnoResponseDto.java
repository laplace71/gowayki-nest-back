package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoResponseDto {
    private UUID id;
    private UUID choferId;
    private String choferNombre;
    private Integer unidadId;
    private String numeroPadron;
    private String placa;
    private Integer lineaId;
    private String lineaNombre;
    private String lineaColorHex;
    private LocalDate fecha;
    private OffsetDateTime horaInicio;
    private OffsetDateTime horaFin;
    private String estado;
    private BigDecimal porcentajeCumplimiento;
    private OffsetDateTime createdAt;
}
