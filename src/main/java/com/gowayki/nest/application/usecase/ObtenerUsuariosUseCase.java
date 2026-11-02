package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.UsuarioResponseDto;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenerUsuariosUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ObtenerUsuariosUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public List<UsuarioResponseDto> obtenerTodos() {
        return usuarioRepositoryPort.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    private UsuarioResponseDto mapToDto(UsuarioModel model) {
        return UsuarioResponseDto.builder()
                .id(model.getId())
                .nombre(model.getNombre())
                .dni(model.getDni())
                .telefono(model.getTelefono())
                .email(model.getEmail())
                .fotoUrl(model.getFotoUrl())
                .rol(model.getRol())
                .isOnline(model.getIsOnline())
                .build();
    }
}