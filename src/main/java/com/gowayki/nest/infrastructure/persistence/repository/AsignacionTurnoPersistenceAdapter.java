package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.AsignacionTurno;
import com.gowayki.nest.infrastructure.persistence.entity.Unidad;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import com.gowayki.nest.infrastructure.persistence.mapper.AsignacionTurnoMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AsignacionTurnoPersistenceAdapter implements AsignacionTurnoRepositoryPort {

    private final SpringDataAsignacionTurnoRepository turnoRepository;
    private final SpringDataUsuarioRepository usuarioRepository;
    private final SpringDataUnidadRepository unidadRepository;
    private final AsignacionTurnoMapper turnoMapper;

    public AsignacionTurnoPersistenceAdapter(SpringDataAsignacionTurnoRepository turnoRepository,
                                            SpringDataUsuarioRepository usuarioRepository,
                                            SpringDataUnidadRepository unidadRepository,
                                            AsignacionTurnoMapper turnoMapper) {
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadRepository = unidadRepository;
        this.turnoMapper = turnoMapper;
    }

    @Override
    @Transactional
    public AsignacionTurnoModel save(AsignacionTurnoModel model) {
        Usuario chofer = usuarioRepository.findById(model.getChoferId())
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado."));

        Unidad unidad = unidadRepository.findById(model.getUnidadId())
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada."));

        AsignacionTurno entity = turnoMapper.toEntity(model, chofer, unidad);
        AsignacionTurno guardado = turnoRepository.save(entity);
        return turnoMapper.toDomain(guardado);
    }

    @Override
    public Optional<AsignacionTurnoModel> findById(UUID id) {
        return turnoRepository.findById(id).map(turnoMapper::toDomain);
    }

    @Override
    public Optional<AsignacionTurnoModel> findTurnoActivoPorChofer(UUID choferId) {
        return turnoRepository.findTurnoActivoPorChofer(choferId).map(turnoMapper::toDomain);
    }

    @Override
    public List<AsignacionTurnoModel> findByChoferId(UUID choferId) {
        return turnoRepository.findByChoferId(choferId).stream()
                .map(turnoMapper::toDomain)
                .toList();
    }

    @Override
    public List<AsignacionTurnoModel> findTurnosActivos() {
        return turnoRepository.findTurnosActivos().stream()
                .map(turnoMapper::toDomain)
                .toList();
    }
}
