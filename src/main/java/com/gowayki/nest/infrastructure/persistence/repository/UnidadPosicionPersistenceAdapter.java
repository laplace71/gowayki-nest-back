package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.UnidadPosicionModel;
import com.gowayki.nest.domain.repository.UnidadPosicionRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Unidad;
import com.gowayki.nest.infrastructure.persistence.entity.UnidadPosicion;
import com.gowayki.nest.infrastructure.persistence.mapper.UnidadPosicionMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UnidadPosicionPersistenceAdapter implements UnidadPosicionRepositoryPort {

    private final SpringDataUnidadPosicionRepository springDataUnidadPosicionRepository;
    private final UnidadPosicionMapper unidadPosicionMapper;
    private final SpringDataUnidadRepository springDataUnidadRepository;

    public UnidadPosicionPersistenceAdapter(SpringDataUnidadPosicionRepository springDataUnidadPosicionRepository,
                                           UnidadPosicionMapper unidadPosicionMapper,
                                           SpringDataUnidadRepository springDataUnidadRepository) {
        this.springDataUnidadPosicionRepository = springDataUnidadPosicionRepository;
        this.unidadPosicionMapper = unidadPosicionMapper;
        this.springDataUnidadRepository = springDataUnidadRepository;
    }

    @Override
    public UnidadPosicionModel save(UnidadPosicionModel model) {
        UnidadPosicion entity = unidadPosicionMapper.toEntity(model);
        if (model.getUnidadId() != null) {
            springDataUnidadRepository.findById(model.getUnidadId()).ifPresent(entity::setUnidad);
        }
        UnidadPosicion guardado = springDataUnidadPosicionRepository.save(entity);
        return unidadPosicionMapper.toDomain(guardado);
    }

    @Override
    public Optional<UnidadPosicionModel> findByUnidadId(Integer unidadId) {
        return springDataUnidadPosicionRepository.findById(unidadId)
                .map(unidadPosicionMapper::toDomain);
    }
}