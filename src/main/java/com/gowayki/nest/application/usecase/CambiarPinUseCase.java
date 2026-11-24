package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CambiarPinUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public CambiarPinUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void ejecutar(UUID usuarioId, String pinActual, String pinNuevo) {
        if (usuarioId == null) {
            throw new NoAutenticadoException("Se requiere un token válido.");
        }
        if (pinActual == null || pinActual.isBlank()) {
            throw new IllegalArgumentException("Indica tu PIN actual.");
        }
        if (pinNuevo == null || !pinNuevo.trim().matches("\\d{6}")) {
            throw new IllegalArgumentException("El nuevo PIN debe tener 6 dígitos.");
        }

        UsuarioModel usuario = usuarioRepositoryPort.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (!passwordEncoder.matches(pinActual.trim(), usuario.getPinHash())) {
            throw new IllegalArgumentException("El PIN actual es incorrecto.");
        }

        usuario.setPinHash(passwordEncoder.encode(pinNuevo.trim()));
        usuarioRepositoryPort.save(usuario);
    }
}
