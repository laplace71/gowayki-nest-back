package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.PuntoControlModel;
import com.gowayki.nest.domain.repository.PuntoControlRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.PuntoControl;
import com.gowayki.nest.infrastructure.persistence.mapper.PuntoControlMapper;
import org.springframework.stereotype.Component;

import java.util.List;

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