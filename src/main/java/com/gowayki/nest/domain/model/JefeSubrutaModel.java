package com.gowayki.nest.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Jefe de ruta con la subruta que el panel le asignó. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JefeSubrutaModel {
    private UUID choferId;
    private String subrutaCodigo;
    private String subrutaNombre;
}
