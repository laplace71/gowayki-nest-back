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
public class UsuarioModel {
    private UUID id;
    private String nombre;
    private String dni;
    private String telefono;
    private String email;
    private String pinHash;
    private String fotoUrl;
    @Builder.Default
    private String rol = "CHOFER";
    @Builder.Default
    private Boolean isOnline = false;
    @Builder.Default
    private Integer intentosFallidos = 0;
    private OffsetDateTime bloqueadoHasta;
    private OffsetDateTime createdAt;
}

