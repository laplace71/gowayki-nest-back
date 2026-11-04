package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ActualizarPosicionGpsRequestDto;
import com.gowayki.nest.application.dto.PosicionGpsDto;
import com.gowayki.nest.domain.model.UnidadPosicionModel;
import com.gowayki.nest.domain.repository.UnidadPosicionRepositoryPort;
import com.gowayki.nest.domain.repository.UnidadRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ActualizarPosicionGpsUseCase {

    private final UnidadPosicionRepositoryPort unidadPosicionRepositoryPort;
    private final UnidadRepositoryPort unidadRepositoryPort;

    public ActualizarPosicionGpsUseCase(UnidadPosicionRepositoryPort unidadPosicionRepositoryPort,
                                       UnidadRepositoryPort unidadRepositoryPort) {
        this.unidadPosicionRepositoryPort = unidadPosicionRepositoryPort;
        this.unidadRepositoryPort = unidadRepositoryPort;
    }

    public PosicionGpsDto ejecutar(Integer unidadId, ActualizarPosicionGpsRequestDto request) {
        if (unidadRepositoryPort.findById(unidadId).isEmpty()) {
            throw new IllegalArgumentException("No existe unidad con ese id.");
        }
        if (request.getLatitud() == null || request.getLongitud() == null) {
            throw new IllegalArgumentException("La latitud y longitud son obligatorias.");
        }

        OffsetDateTime ahora = OffsetDateTime.now();

        UnidadPosicionModel model = UnidadPosicionModel.builder()
                .unidadId(unidadId)
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .velocidadKmh(request.getVelocidadKmh())
                .rumboGrados(request.getRumboGrados())
                .updatedAt(ahora)
                .build();

        UnidadPosicionModel guardado = unidadPosicionRepositoryPort.save(model);

        return PosicionGpsDto.builder()
                .latitud(guardado.getLatitud())
                .longitud(guardado.getLongitud())
                .velocidadKmh(guardado.getVelocidadKmh())
                .rumboGrados(guardado.getRumboGrados())
                .ultimaActualizacion(guardado.getUpdatedAt().toString())
                .build();
    }
}