package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.UsuarioModel;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {
    Optional<UsuarioModel> findById(UUID id);
    Optional<UsuarioModel> findByDni(String dni);
    Optional<UsuarioModel> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByTelefono(String telefono);
    UsuarioModel save(UsuarioModel usuario);
}
