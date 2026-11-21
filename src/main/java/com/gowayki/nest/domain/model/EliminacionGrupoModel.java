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
public class EliminacionGrupoModel {
    private UUID id;
    private UUID conversacionId;
    private String tituloSnapshot;
    private UUID solicitadoPor;
    private OffsetDateTime solicitadoEn;
    private OffsetDateTime ejecutarDespues;
    private String estado;
    private UUID canceladaPor;
    private OffsetDateTime canceladaEn;
    private OffsetDateTime ejecutadaEn;
}
