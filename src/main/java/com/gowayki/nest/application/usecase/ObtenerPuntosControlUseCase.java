package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.PuntoControlDto;
import com.gowayki.nest.domain.model.PuntoControlModel;
import com.gowayki.nest.domain.repository.PuntoControlRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenerPuntosControlUseCase {

    private final PuntoControlRepositoryPort puntoControlRepositoryPort;

    public ObtenerPuntosControlUseCase(PuntoControlRepositoryPort puntoControlRepositoryPort) {
        this.puntoControlRepositoryPort = puntoControlRepositoryPort;
    }

    public List<PuntoControlDto> obtenerPorLinea(Integer lineaId) {
        return puntoControlRepositoryPort.findByLineaId(lineaId).stream()
                .map(this::mapToDto)
                .toList();
    }

    private PuntoControlDto mapToDto(PuntoControlModel model) {
        return PuntoControlDto.builder()
                .id(model.getId())
                .nombre(model.getNombre())
                .orden(model.getOrden())
                .latitud(model.getLatitud())
                .longitud(model.getLongitud())
                .radioToleranciaMetros(model.getRadioToleranciaMetros())
                .build();
    }
}