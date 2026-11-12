package com.gowayki.nest.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.gowayki.nest.domain.model.PuntoControlModel;
import com.gowayki.nest.domain.repository.PuntoControlRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.mapper.PuntoControlMapper;

@Component
public class PuntoControlPersistenceAdapter implements PuntoControlRepositoryPort {

    private final SpringDataPuntoControlRepository springDataPuntoControlRepository;
    private final PuntoControlMapper puntoControlMapper;

    public PuntoControlPersistenceAdapter(SpringDataPuntoControlRepository springDataPuntoControlRepository,
                                          PuntoControlMapper puntoControlMapper) {
        this.springDataPuntoControlRepository = springDataPuntoControlRepository;
        this.puntoControlMapper = puntoControlMapper;
    }

    @Override
    public List<PuntoControlModel> findByLineaId(Integer lineaId) {
        return springDataPuntoControlRepository.findByLineaIdOrderByOrdenAsc(lineaId).stream()
                .map(puntoControlMapper::toDomain)
                .toList();
    }
}