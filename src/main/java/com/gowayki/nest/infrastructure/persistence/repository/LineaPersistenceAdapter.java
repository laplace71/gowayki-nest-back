package com.gowayki.nest.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.gowayki.nest.domain.model.LineaModel;
import com.gowayki.nest.domain.repository.LineaRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.PuntoControl;
import com.gowayki.nest.infrastructure.persistence.mapper.LineaMapper;

@Component
public class LineaPersistenceAdapter implements LineaRepositoryPort {

    private final SpringDataLineaRepository lineaRepository;
    private final SpringDataPuntoControlRepository puntoControlRepository;
    private final LineaMapper lineaMapper;

    public LineaPersistenceAdapter(
            SpringDataLineaRepository lineaRepository,
            SpringDataPuntoControlRepository puntoControlRepository,
            LineaMapper lineaMapper
    ) {
        this.lineaRepository = lineaRepository;
        this.puntoControlRepository = puntoControlRepository;
        this.lineaMapper = lineaMapper;
    }

    @Override
    public List<LineaModel> findAll() {
        return lineaRepository.findAll().stream()
                .map(linea -> {
                    List<PuntoControl> puntos = puntoControlRepository.findByLineaIdOrderByOrdenAsc(linea.getId());
                    return lineaMapper.toDomain(linea, puntos);
                })
                .toList();
    }

    @Override
    public Optional<LineaModel> findById(Integer id) {
        return lineaRepository.findById(id)
                .map(linea -> {
                    List<PuntoControl> puntos = puntoControlRepository.findByLineaIdOrderByOrdenAsc(linea.getId());
                    return lineaMapper.toDomain(linea, puntos);
                });
    }
}

