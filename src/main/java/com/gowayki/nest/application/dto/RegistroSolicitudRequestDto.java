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
public class RegistroSolicitudRequestDto {
    private String nombre;
    private String dni;
    private String telefono;
    private String email;
    private String pin;
    private Integer lineaId;
    private Integer subrutaId;
}
