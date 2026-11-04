package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import com.gowayki.nest.infrastructure.persistence.entity.Mensaje;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class MensajeMapper {

    public MensajeModel toDomain(Mensaje entity) {
        if (entity == null) {
            return null;
        }

        return MensajeModel.builder()
                .id(entity.getId())
                .conversacionId(entity.getConversacion() != null ? entity.getConversacion().getId() : null)
                .emisorId(entity.getEmisor() != null ? entity.getEmisor().getId() : null)
                .emisorNombre(entity.getEmisor() != null ? entity.getEmisor().getNombre() : null)
                .tipo(entity.getTipo())
                .contenidoTexto(entity.getContenidoTexto())
                .audioUrl(entity.getAudioUrl())
                .audioDuracionSegundos(entity.getAudioDuracionSegundos())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public Mensaje toEntity(MensajeModel model, Conversacion conversacion, Usuario emisor) {
        if (model == null) {
            return null;
        }

        return Mensaje.builder()
                .id(model.getId())
                .conversacion(conversacion)
                .emisor(emisor)
                .tipo(model.getTipo())
                .contenidoTexto(model.getContenidoTexto())
                .audioUrl(model.getAudioUrl())
                .audioDuracionSegundos(model.getAudioDuracionSegundos() != null ? model.getAudioDuracionSegundos() : 0)
                .estado(model.getEstado() != null ? model.getEstado() : "ENVIADO")
                .createdAt(model.getCreatedAt())
                .build();
    }
}

