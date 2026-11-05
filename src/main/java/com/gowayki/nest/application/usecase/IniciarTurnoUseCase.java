package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.TurnoResponseDto;
import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class IniciarTurnoUseCase {

    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;

    public IniciarTurnoUseCase(AsignacionTurnoRepositoryPort turnoRepositoryPort) {
        this.turnoRepositoryPort = turnoRepositoryPort;
    }

    public TurnoResponseDto ejecutar(UUID turnoId) {
        AsignacionTurnoModel turno = turnoRepositoryPort.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("El turno no existe."));

        turno.setEstado("EN_RUTA");
        turno.setHoraInicio(OffsetDateTime.now());

        AsignacionTurnoModel actualizado = turnoRepositoryPort.save(turno);

        return TurnoResponseDto.builder()
                .id(actualizado.getId())
                .choferId(actualizado.getChoferId())
                .choferNombre(actualizado.getChoferNombre())
                .unidadId(actualizado.getUnidadId())
                .numeroPadron(actualizado.getNumeroPadron())
                .placa(actualizado.getPlaca())
                .lineaId(actualizado.getLineaId())
                .lineaNombre(actualizado.getLineaNombre())
                .lineaColorHex(actualizado.getLineaColorHex())
                .fecha(actualizado.getFecha())
                .horaInicio(actualizado.getHoraInicio())
                .horaFin(actualizado.getHoraFin())
                .estado(actualizado.getEstado())
                .porcentajeCumplimiento(actualizado.getPorcentajeCumplimiento())
                .createdAt(actualizado.getCreatedAt())
                .build();
    }
}
