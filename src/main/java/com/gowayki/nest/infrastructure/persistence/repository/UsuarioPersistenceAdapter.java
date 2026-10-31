package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import com.gowayki.nest.infrastructure.persistence.mapper.UsuarioMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository springDataUsuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioPersistenceAdapter(SpringDataUsuarioRepository springDataUsuarioRepository,
                                     UsuarioMapper usuarioMapper) {
        this.springDataUsuarioRepository = springDataUsuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public Optional<UsuarioModel> findById(UUID id) {
        return springDataUsuarioRepository.findById(id)
                .map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<UsuarioModel> findByDni(String dni) {
        return springDataUsuarioRepository.findByDni(dni)
                .map(usuarioMapper::toDomain);
    }

    @Override
    public Optional<UsuarioModel> findByEmail(String email) {
        return springDataUsuarioRepository.findByEmail(email)
                .map(usuarioMapper::toDomain);
    }

    @Override
    public boolean existsByDni(String dni) {
        return springDataUsuarioRepository.existsByDni(dni);
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        return springDataUsuarioRepository.existsByTelefono(telefono);
    }

    @Override
    public UsuarioModel save(UsuarioModel usuario) {
        Usuario entity = usuarioMapper.toEntity(usuario);
        Usuario guardado = springDataUsuarioRepository.save(entity);
        return usuarioMapper.toDomain(guardado);
    }
}

