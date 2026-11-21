package com.gowayki.nest.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Archivo a borrar del disco cuando la transacción ya haya hecho commit.
 * No es un mensaje ni una conversación: es solo la intención de borrar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoPendienteBorradoModel {
    private UUID id;
    private String ruta;
    private String motivo;
    private OffsetDateTime creadoEn;
    private Integer intentos;
    private OffsetDateTime ultimoIntentoEn;
    private String ultimoError;
}
