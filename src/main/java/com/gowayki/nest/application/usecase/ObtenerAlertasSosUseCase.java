package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.AlertaSosResponseDto;
import com.gowayki.nest.domain.model.AlertaIncidenciaModel;
import com.gowayki.nest.domain.repository.AlertaIncidenciaRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenerAlertasSosUseCase {

    private final AlertaIncidenciaRepositoryPort alertaIncidenciaRepositoryPort;

    public ObtenerAlertasSosUseCase(AlertaIncidenciaRepositoryPort alertaIncidenciaRepositoryPort) {
        this.alertaIncidenciaRepositoryPort = alertaIncidenciaRepositoryPort;
    }

    public List<AlertaSosResponseDto> listarTodas() {
        return alertaIncidenciaRepositoryPort.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<AlertaSosResponseDto> listarActivas() {
        return alertaIncidenciaRepositoryPort.findByEstado("ACTIVA").stream()
                .map(this::mapToDto)
                .toList();
    }

    private AlertaSosResponseDto mapToDto(AlertaIncidenciaModel model) {
        return AlertaSosResponseDto.builder()
                .id(model.getId())
                .choferId(model.getChoferId())
                .unidadId(model.getUnidadId())
                .tipo(model.getTipo())
                .origenDisparo(model.getOrigenDisparo())
                .latitud(model.getLatitud())
                .longitud(model.getLongitud())
                .descripcion(model.getDescripcion())
                .audioUrl(model.getAudioUrl())
                .estado(model.getEstado())
                .createdAt(model.getCreatedAt())
                .expiraAt(model.getExpiraAt())
                .build();
    }
}