package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.AuthResponseDto;
import com.gowayki.nest.application.dto.LoginRequestDto;
import com.gowayki.nest.application.dto.UsuarioResponseDto;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import com.gowayki.nest.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class LoginUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginUseCase(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
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

        // Cuenta eliminada (borrado lógico): no se puede entrar, pero la fila y
        // sus mensajes se conservan. El re-registro por DNI la reactiva.
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException(
                    "Esta cuenta fue eliminada. Vuelve a registrarte con tu DNI y tu PIN anterior para restaurarla."
            );
        }

        // Validar si la cuenta está bloqueada temporalmente
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(OffsetDateTime.now())) {
            throw new IllegalStateException("Cuenta bloqueada temporalmente por exceso de intentos fallidos. Intente más tarde.");
        }

        // Validar PIN
        boolean pinValido = passwordEncoder.matches(request.getPin().trim(), usuario.getPinHash());
        if (!pinValido) {
            int intentos = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;
            usuario.setIntentosFallidos(intentos);

            OffsetDateTime bloqueado = intentos >= 5
                    ? OffsetDateTime.now().plusMinutes(15) : null;
            // Actualización parcial: nunca se guarda el modelo leído al inicio,
            // porque podría sobrescribir un borrado de cuenta concurrente.
            usuarioRepository.registrarIntentoFallido(usuario.getId(), intentos, bloqueado);

            int restantes = Math.max(0, 5 - intentos);
            throw new IllegalArgumentException("PIN incorrecto. Intentos restantes antes de bloqueo temporal: " + restantes);
        }

        // Login exitoso: actualizaciones parciales + revalidación de estado.
        usuarioRepository.marcarLoginExitoso(usuario.getId());
        if (!usuarioRepository.estaActivo(usuario.getId())) {
            throw new IllegalArgumentException("Esta cuenta fue eliminada. No se puede iniciar sesión.");
        }
        UsuarioModel actualizado = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario ya no existe."));

        return AuthResponseDto.builder()
                .exito(true)
                .mensaje("Inicio de sesión exitoso.")
                .usuario(toResponseDto(actualizado))
                .token(jwtService.generar(
                        actualizado.getId(), actualizado.getRol(), actualizado.getTokenVersion()))
                .build();
    }

    private UsuarioResponseDto toResponseDto(UsuarioModel usuario) {
        var subruta = usuarioRepository.findUsuariosConSubruta().stream()
                .filter(j -> usuario.getId().equals(j.getChoferId()))
                .findFirst()
                .orElse(null);
        return UsuarioResponseDto.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .dni(usuario.getDni())
                .telefono(usuario.getTelefono())
                .email(usuario.getEmail())
                .fotoUrl(usuario.getFotoUrl())
                .rol(usuario.getRol())
                .isOnline(usuario.getIsOnline())
                .subrutaId(subruta == null ? null : subruta.getSubrutaCodigo())
                .subrutaNombre(subruta == null ? null : subruta.getSubrutaNombre())
                .build();
    }
}

