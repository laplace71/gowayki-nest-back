package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.AuthResponseDto;
import com.gowayki.nest.application.dto.RegistroUsuarioRequestDto;
import com.gowayki.nest.application.dto.UsuarioResponseDto;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class RegistroUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroUsuarioUseCase(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto ejecutar(RegistroUsuarioRequestDto request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (request.getDni() == null || request.getDni().trim().length() != 8) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }
        if (request.getTelefono() == null || request.getTelefono().trim().length() != 9) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 9 dígitos.");
        }
        if (request.getPin() == null || request.getPin().trim().length() < 4) {
            throw new IllegalArgumentException("El PIN debe tener al menos 4 dígitos numéricos.");
        }

        String dni = request.getDni().trim();
        String telefono = request.getTelefono().trim();

        if (usuarioRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("El DNI ya está registrado en el sistema.");
        }
        if (usuarioRepository.existsByTelefono(telefono)) {
            throw new IllegalArgumentException("El número de teléfono ya está registrado en el sistema.");
        }

        String pinHash = passwordEncoder.encode(request.getPin().trim());
        String rol = (request.getRol() == null || request.getRol().isBlank()) ? "CHOFER" : request.getRol().trim().toUpperCase();

        UsuarioModel nuevoUsuario = UsuarioModel.builder()
                .nombre(request.getNombre().trim())
                .dni(dni)
                .telefono(telefono)
                .email(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null)
                .pinHash(pinHash)
                .fotoUrl(request.getFotoUrl())
                .rol(rol)
                .isOnline(false)
                .intentosFallidos(0)
                .createdAt(OffsetDateTime.now())
                .build();

        UsuarioModel guardado = usuarioRepository.save(nuevoUsuario);

        return AuthResponseDto.builder()
                .exito(true)
                .mensaje("Usuario registrado exitosamente.")
                .usuario(UsuarioResponseDto.builder()
                        .id(guardado.getId())
                        .nombre(guardado.getNombre())
                        .dni(guardado.getDni())
                        .telefono(guardado.getTelefono())
                        .email(guardado.getEmail())
                        .fotoUrl(guardado.getFotoUrl())
                        .rol(guardado.getRol())
                        .isOnline(guardado.getIsOnline())
                        .build())
                .build();
    }
}
