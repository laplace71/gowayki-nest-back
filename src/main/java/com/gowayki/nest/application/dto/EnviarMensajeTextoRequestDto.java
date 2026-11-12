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
public class EnviarMensajeTextoRequestDto {
    private UUID emisorId;
    private String contenidoTexto;

    public void setContenido(String contenido) {
        if (this.contenidoTexto == null || this.contenidoTexto.isBlank()) {
            this.contenidoTexto = contenido;
        }
    }
}

