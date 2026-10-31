package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDto {
    private UUID id;
    private String nombre;
    private String dni;
    private String telefono;
    private String email;
    private String fotoUrl;
    private String rol;
    private Boolean isOnline;
}

