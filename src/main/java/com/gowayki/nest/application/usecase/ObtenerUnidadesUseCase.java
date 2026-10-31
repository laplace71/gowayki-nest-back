package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.PosicionGpsDto;
import com.gowayki.nest.application.dto.UnidadResponseDto;
import com.gowayki.nest.domain.model.PosicionGpsModel;
import com.gowayki.nest.domain.model.UnidadModel;
import com.gowayki.nest.domain.repository.UnidadRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ObtenerUnidadesUseCase {

    private final UnidadRepositoryPort unidadRepositoryPort;

    public ObtenerUnidadesUseCase(UnidadRepositoryPort unidadRepositoryPort) {
        this.unidadRepositoryPort = unidadRepositoryPort;
    }

    public List<UnidadResponseDto> obtenerTodas() {
        return unidadRepositoryPort.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<UnidadResponseDto> obtenerPorLinea(Integer lineaId) {
        return unidadRepositoryPort.findByLineaId(lineaId).stream()
                .map(this::mapToDto)
                .toList();
    }

    public Optional<UnidadResponseDto> obtenerPorId(Integer id) {
        return unidadRepositoryPort.findById(id)
                .map(this::mapToDto);
    }

    private UnidadResponseDto mapToDto(UnidadModel model) {
        PosicionGpsDto posicionDto = null;
        if (model.getPosicionActual() != null) {
            PosicionGpsModel p = model.getPosicionActual();
            posicionDto = PosicionGpsDto.builder()
                    .latitud(p.getLatitud())
                    .longitud(p.getLongitud())
                    .velocidadKmh(p.getVelocidadKmh())
                    .rumboGrados(p.getRumboGrados())
                    .ultimaActualizacion(p.getUltimaActualizacion())
                    .build();
        }

        return UnidadResponseDto.builder()
                .id(model.getId())
                .numeroPadron(model.getNumeroPadron())
                .placa(model.getPlaca())
                .lineaId(model.getLineaId())
                .lineaNombre(model.getLineaNombre())
                .estado(model.getEstado())
                .posicionActual(posicionDto)
                .build();
    }
}

