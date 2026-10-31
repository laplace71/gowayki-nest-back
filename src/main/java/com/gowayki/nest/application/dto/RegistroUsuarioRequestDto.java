package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroUsuarioRequestDto {
    private String nombre;
    private String dni;
    private String telefono;
    private String email;
    private String pin;
    private String fotoUrl;
    private String rol;
}
