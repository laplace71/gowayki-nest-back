package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.UnidadModel;
import com.gowayki.nest.domain.repository.UnidadRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Unidad;
import com.gowayki.nest.infrastructure.persistence.entity.UnidadPosicion;
import com.gowayki.nest.infrastructure.persistence.mapper.UnidadMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UnidadPersistenceAdapter implements UnidadRepositoryPort {

    private final SpringDataUnidadRepository unidadRepository;
    private final SpringDataUnidadPosicionRepository unidadPosicionRepository;
    private final UnidadMapper unidadMapper;

    public UnidadPersistenceAdapter(
            SpringDataUnidadRepository unidadRepository,
            SpringDataUnidadPosicionRepository unidadPosicionRepository,
            UnidadMapper unidadMapper
    ) {
        this.unidadRepository = unidadRepository;
        this.unidadPosicionRepository = unidadPosicionRepository;
        this.unidadMapper = unidadMapper;
    }

    @Override
    public List<UnidadModel> findAll() {
        return unidadRepository.findAllWithLinea().stream()
                .map(this::mapearConPosicion)
                .toList();
    }

    @Override
    public List<UnidadModel> findByLineaId(Integer lineaId) {
        return unidadRepository.findByLineaId(lineaId).stream()
                .map(this::mapearConPosicion)
                .toList();
    }

    @Override
    public Optional<UnidadModel> findById(Integer id) {
        return unidadRepository.findById(id)
                .map(this::mapearConPosicion);
    }

    private UnidadModel mapearConPosicion(Unidad unidad) {
        UnidadPosicion pos = unidadPosicionRepository.findById(unidad.getId()).orElse(null);
        return unidadMapper.toDomain(unidad, pos);
    }
}

