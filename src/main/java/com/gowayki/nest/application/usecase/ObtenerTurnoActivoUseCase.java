package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.TurnoResponseDto;
import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ObtenerTurnoActivoUseCase {

    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;

    public ObtenerTurnoActivoUseCase(AsignacionTurnoRepositoryPort turnoRepositoryPort) {
        this.turnoRepositoryPort = turnoRepositoryPort;
    }

    public Optional<TurnoResponseDto> obtenerPorChofer(UUID choferId) {
        return turnoRepositoryPort.findTurnoActivoPorChofer(choferId)
                .map(this::toDto);
    }

    public List<TurnoResponseDto> listarTodosActivos() {
        return turnoRepositoryPort.findTurnosActivos().stream()
                .map(this::toDto)
                .toList();
    }

    private TurnoResponseDto toDto(AsignacionTurnoModel model) {
        return TurnoResponseDto.builder()
                .id(model.getId())
                .choferId(model.getChoferId())
                .choferNombre(model.getChoferNombre())
                .unidadId(model.getUnidadId())
                .numeroPadron(model.getNumeroPadron())
                .placa(model.getPlaca())
                .lineaId(model.getLineaId())
                .lineaNombre(model.getLineaNombre())
                .lineaColorHex(model.getLineaColorHex())
                .fecha(model.getFecha())
                .horaInicio(model.getHoraInicio())
                .horaFin(model.getHoraFin())
                .estado(model.getEstado())
                .porcentajeCumplimiento(model.getPorcentajeCumplimiento())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
