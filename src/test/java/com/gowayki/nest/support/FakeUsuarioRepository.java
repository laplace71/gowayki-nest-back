package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Usuarios en memoria para los casos de uso que validan al creador. */
public class FakeUsuarioRepository implements UsuarioRepositoryPort {

    private final Map<UUID, UsuarioModel> usuarios = new LinkedHashMap<>();

    public UUID agregar(String nombre) {
        UUID id = UUID.randomUUID();
        usuarios.put(id, UsuarioModel.builder().id(id).nombre(nombre).build());
        return id;
    }

    @Override
    public Optional<UsuarioModel> findById(UUID id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<UsuarioModel> findByDni(String dni) {
        return usuarios.values().stream().filter(u -> dni.equals(u.getDni())).findFirst();
    }

    @Override
    public Optional<UsuarioModel> findByEmail(String email) {
        return usuarios.values().stream().filter(u -> email.equals(u.getEmail())).findFirst();
    }

    @Override
    public boolean existsByDni(String dni) {
        return findByDni(dni).isPresent();
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        return usuarios.values().stream().anyMatch(u -> telefono.equals(u.getTelefono()));
    }

    @Override
    public UsuarioModel save(UsuarioModel usuario) {
        UUID id = usuario.getId() != null ? usuario.getId() : UUID.randomUUID();
        usuario.setId(id);
        usuarios.put(id, usuario);
        return usuario;
    }

    @Override
    public List<UsuarioModel> findAll() {
        return new ArrayList<>(usuarios.values());
    }
}
