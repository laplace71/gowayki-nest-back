package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.AuthResponseDto;
import com.gowayki.nest.application.dto.LoginRequestDto;
import com.gowayki.nest.application.dto.UsuarioResponseDto;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class LoginUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUseCase(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto ejecutar(LoginRequestDto request) {
        if (request.getDni() == null || request.getDni().isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }
        if (request.getPin() == null || request.getPin().isBlank()) {
            throw new IllegalArgumentException("El PIN es obligatorio.");
        }

        String identificador = request.getDni().trim();
        Optional<UsuarioModel> usuarioOpt = usuarioRepository.findByDni(identificador);
        if (usuarioOpt.isEmpty() && identificador.contains("@")) {
            usuarioOpt = usuarioRepository.findByEmail(identificador);
        }

        UsuarioModel usuario = usuarioOpt.orElseThrow(() ->
                new IllegalArgumentException("Credenciales inválidas: No existe usuario registrado con ese identificador.")
        );

        // Validar si la cuenta está bloqueada temporalmente
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(OffsetDateTime.now())) {
            throw new IllegalStateException("Cuenta bloqueada temporalmente por exceso de intentos fallidos. Intente más tarde.");
        }

        // Validar PIN
        boolean pinValido = passwordEncoder.matches(request.getPin().trim(), usuario.getPinHash());
        if (!pinValido) {
            int intentos = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;
            usuario.setIntentosFallidos(intentos);

            if (intentos >= 5) {
                usuario.setBloqueadoHasta(OffsetDateTime.now().plusMinutes(15));
            }
            usuarioRepository.save(usuario);

            int restantes = Math.max(0, 5 - intentos);
            throw new IllegalArgumentException("PIN incorrecto. Intentos restantes antes de bloqueo temporal: " + restantes);
        }

        // Login exitoso: restablecer intentos y marcar online
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setIsOnline(true);
        UsuarioModel actualizado = usuarioRepository.save(usuario);

        return AuthResponseDto.builder()
                .exito(true)
                .mensaje("Inicio de sesión exitoso.")
                .usuario(toResponseDto(actualizado))
                .build();
    }

    private UsuarioResponseDto toResponseDto(UsuarioModel usuario) {
        return UsuarioResponseDto.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .dni(usuario.getDni())
                .telefono(usuario.getTelefono())
                .email(usuario.getEmail())
                .fotoUrl(usuario.getFotoUrl())
                .rol(usuario.getRol())
                .isOnline(usuario.getIsOnline())
                .build();
    }
}

