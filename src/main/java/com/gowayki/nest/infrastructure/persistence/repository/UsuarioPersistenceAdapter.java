package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import com.gowayki.nest.infrastructure.persistence.mapper.UsuarioMapper;
import org.springframework.stereotype.Component;

import java.util.List;
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

    @Override
    public List<UsuarioModel> findAll() {
        return springDataUsuarioRepository.findAll().stream()
                .map(usuarioMapper::toDomain)
                .toList();
    }

    @Override
    public List<UsuarioModel> findAllActivos() {
        return springDataUsuarioRepository.findByActivoTrue().stream()
                .map(usuarioMapper::toDomain)
                .toList();
    }

    @Override
    public List<com.gowayki.nest.domain.model.JefeSubrutaModel> findJefesConSubruta() {
        return springDataUsuarioRepository.findJefesConSubruta().stream()
                .map(fila -> com.gowayki.nest.domain.model.JefeSubrutaModel.builder()
                        .choferId((UUID) fila[0])
                        .subrutaCodigo(fila[1] == null ? null : fila[1].toString())
                        .subrutaNombre(fila[2] == null ? null : fila[2].toString())
                        .build())
                .toList();
    }

    @Override
    public List<com.gowayki.nest.domain.model.JefeSubrutaModel> findUsuariosConSubruta() {
        return springDataUsuarioRepository.findUsuariosConSubruta().stream()
                .map(fila -> com.gowayki.nest.domain.model.JefeSubrutaModel.builder()
                        .choferId((UUID) fila[0])
                        .subrutaCodigo(fila[1] == null ? null : fila[1].toString())
                        .subrutaNombre(fila[2] == null ? null : fila[2].toString())
                        .build())
                .toList();
    }

    @Override
    public void registrarIntentoFallido(UUID id, int intentos, java.time.OffsetDateTime bloqueadoHasta) {
        springDataUsuarioRepository.registrarIntentoFallido(id, intentos, bloqueadoHasta);
    }

    @Override
    public void marcarLoginExitoso(UUID id) {
        springDataUsuarioRepository.marcarLoginExitoso(id);
    }

    @Override
    public boolean estaActivo(UUID id) {
        Boolean activo = springDataUsuarioRepository.estaActivo(id);
        return Boolean.TRUE.equals(activo);
    }

    @Override
    public void incrementarTokenVersion(UUID id) {
        springDataUsuarioRepository.incrementarTokenVersion(id);
    }
}

