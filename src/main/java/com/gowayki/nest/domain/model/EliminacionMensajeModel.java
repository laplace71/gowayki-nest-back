package com.gowayki.nest.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EliminacionMensajeModel {
    private UUID id;
    private UUID mensajeId;
    private UUID conversacionId;
    private UUID solicitadoPor;
    private OffsetDateTime solicitadoEn;
    private OffsetDateTime ejecutarDespues;
    private String estado;
    private OffsetDateTime ejecutadaEn;
}
