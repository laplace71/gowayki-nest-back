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

    @Override
    public void incrementarTokenVersion(UUID id) {
        UsuarioModel u = usuarios.get(id);
        if (u != null) {
            u.setTokenVersion((u.getTokenVersion() == null ? 0 : u.getTokenVersion()) + 1);
        }
    }

    @Override
    public boolean estaActivo(UUID id) {
        UsuarioModel u = usuarios.get(id);
        return u != null && !Boolean.FALSE.equals(u.getActivo());
    }

    @Override
    public List<com.gowayki.nest.domain.model.JefeSubrutaModel> findJefesConSubruta() {
        return usuarios.values().stream()
                .filter(u -> "JEFE_RUTA".equalsIgnoreCase(u.getRol()))
                .filter(u -> !Boolean.FALSE.equals(u.getActivo()))
                .filter(u -> u.getSubrutaCodigo() != null && !u.getSubrutaCodigo().isBlank())
                .map(u -> com.gowayki.nest.domain.model.JefeSubrutaModel.builder()
                        .choferId(u.getId())
                        .subrutaCodigo(u.getSubrutaCodigo())
                        .subrutaNombre(u.getSubrutaNombre())
                        .build())
                .toList();
    }

    @Override
    public List<UsuarioModel> findAllActivos() {
        return usuarios.values().stream()
                .filter(u -> !Boolean.FALSE.equals(u.getActivo()))
                .toList();
    }

    @Override
    public void registrarIntentoFallido(UUID id, int intentos, java.time.OffsetDateTime bloqueadoHasta) {
        UsuarioModel u = usuarios.get(id);
        if (u != null) {
            u.setIntentosFallidos(intentos);
            u.setBloqueadoHasta(bloqueadoHasta);
        }
    }

    @Override
    public void marcarLoginExitoso(UUID id) {
        UsuarioModel u = usuarios.get(id);
        if (u != null) {
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
        }
    }
}
