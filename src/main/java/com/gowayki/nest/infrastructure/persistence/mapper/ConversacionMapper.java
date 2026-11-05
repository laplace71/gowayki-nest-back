package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ConversacionMapper {

    public ConversacionModel toDomain(Conversacion entity) {
        if (entity == null) {
            return null;
        }

        return ConversacionModel.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .esGrupo(entity.getEsGrupo())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public Conversacion toEntity(ConversacionModel model, Usuario createdBy) {
        if (model == null) {
            return null;
        }

        return Conversacion.builder()
                .id(model.getId())
                .titulo(model.getTitulo())
                .esGrupo(model.getEsGrupo())
                .createdBy(createdBy)
                .createdAt(model.getCreatedAt())
                .build();
    }
}

