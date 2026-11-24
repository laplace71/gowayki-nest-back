package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.UsuarioModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {
    Optional<UsuarioModel> findById(UUID id);
    Optional<UsuarioModel> findByDni(String dni);
    Optional<UsuarioModel> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByTelefono(String telefono);
    UsuarioModel save(UsuarioModel usuario);

    List<UsuarioModel> findAll();

    List<UsuarioModel> findAllActivos();

    /** Jefes de ruta activos con la subruta asignada desde el panel. */
    List<com.gowayki.nest.domain.model.JefeSubrutaModel> findJefesConSubruta();

    /** Usuarios activos (jefe o chofer) con su subruta asignada. */
    List<com.gowayki.nest.domain.model.JefeSubrutaModel> findUsuariosConSubruta();

    /** Actualizaciones parciales: no sobrescriben el estado de la cuenta. */
    void registrarIntentoFallido(UUID id, int intentos, java.time.OffsetDateTime bloqueadoHasta);

    void marcarLoginExitoso(UUID id);

    boolean estaActivo(UUID id);

    void incrementarTokenVersion(UUID id);
}

