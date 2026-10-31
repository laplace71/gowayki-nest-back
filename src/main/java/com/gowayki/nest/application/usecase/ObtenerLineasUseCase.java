package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.LineaResponseDto;
import com.gowayki.nest.application.dto.PuntoControlDto;
import com.gowayki.nest.domain.model.LineaModel;
import com.gowayki.nest.domain.repository.LineaRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObtenerLineasUseCase {

    private final LineaRepositoryPort lineaRepositoryPort;

    public ObtenerLineasUseCase(LineaRepositoryPort lineaRepositoryPort) {
        this.lineaRepositoryPort = lineaRepositoryPort;
    }

    public List<LineaResponseDto> ejecutar() {
        return lineaRepositoryPort.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public Optional<LineaResponseDto> ejecutarPorId(Integer id) {
        return lineaRepositoryPort.findById(id)
                .map(this::mapToDto);
    }

    private LineaResponseDto mapToDto(LineaModel model) {
        List<PuntoControlDto> paraderosDto = model.getPuntosControl().stream()
                .map(p -> PuntoControlDto.builder()
                        .id(p.getId())
                        .nombre(p.getNombre())
                        .orden(p.getOrden())
                        .latitud(p.getLatitud())
                        .longitud(p.getLongitud())
                        .radioToleranciaMetros(p.getRadioToleranciaMetros())
                        .build())
                .toList();

        return LineaResponseDto.builder()
                .id(model.getId())
                .nombre(model.getNombre())
                .origen(model.getOrigen())
                .destino(model.getDestino())
                .corredor(model.getCorredor())
                .colorHex(model.getColorHex())
                .paraderos(paraderosDto)
                .build();
    }
}

