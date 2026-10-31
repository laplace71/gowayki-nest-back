package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(UsuarioModel model) {
        if (model == null) return null;
        return Usuario.builder()
                .id(model.getId())
                .nombre(model.getNombre())
                .dni(model.getDni())
                .telefono(model.getTelefono())
                .email(model.getEmail())
                .pinHash(model.getPinHash())
                .fotoUrl(model.getFotoUrl())
                .rol(model.getRol())
                .isOnline(model.getIsOnline())
                .intentosFallidos(model.getIntentosFallidos())
                .bloqueadoHasta(model.getBloqueadoHasta())
                .createdAt(model.getCreatedAt())
                .build();
    }

    public UsuarioModel toDomain(Usuario entity) {
        if (entity == null) return null;
        return UsuarioModel.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .dni(entity.getDni())
                .telefono(entity.getTelefono())
                .email(entity.getEmail())
                .pinHash(entity.getPinHash())
                .fotoUrl(entity.getFotoUrl())
                .rol(entity.getRol())
                .isOnline(entity.getIsOnline())
                .intentosFallidos(entity.getIntentosFallidos())
                .bloqueadoHasta(entity.getBloqueadoHasta())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
