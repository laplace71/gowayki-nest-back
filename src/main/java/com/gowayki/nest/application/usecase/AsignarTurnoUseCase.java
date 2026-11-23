package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.AsignarTurnoRequestDto;
import com.gowayki.nest.application.dto.TurnoResponseDto;
import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.model.UnidadModel;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import com.gowayki.nest.domain.repository.UnidadRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class AsignarTurnoUseCase {

    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final UnidadRepositoryPort unidadRepositoryPort;

    public AsignarTurnoUseCase(AsignacionTurnoRepositoryPort turnoRepositoryPort,
                               UsuarioRepositoryPort usuarioRepositoryPort,
                               UnidadRepositoryPort unidadRepositoryPort) {
        this.turnoRepositoryPort = turnoRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.unidadRepositoryPort = unidadRepositoryPort;
    }

    @Transactional
    public TurnoResponseDto ejecutar(AsignarTurnoRequestDto request) {
        if (request.getChoferId() == null) {
            throw new IllegalArgumentException("El id del chofer es obligatorio.");
        }
        if (request.getUnidadId() == null) {
            throw new IllegalArgumentException("El id de la unidad es obligatorio.");
        }

        UsuarioModel chofer = usuarioRepositoryPort.findById(request.getChoferId())
                .orElseThrow(() -> new IllegalArgumentException("El chofer no existe."));
        if (!Boolean.TRUE.equals(chofer.getActivo())) {
            throw new IllegalArgumentException("El chofer no está activo.");
        }

        UnidadModel unidad = unidadRepositoryPort.findById(request.getUnidadId())
                .orElseThrow(() -> new IllegalArgumentException("La unidad no existe."));

        if (turnoRepositoryPort.findTurnoActivoPorChofer(chofer.getId()).isPresent()) {
            throw new IllegalStateException("El chofer ya tiene un turno activo o programado.");
        }

        LocalDate fecha = request.getFecha() != null ? request.getFecha() : LocalDate.now();
        OffsetDateTime horaInicio = request.getHoraInicio() != null ? request.getHoraInicio() : OffsetDateTime.now();

        AsignacionTurnoModel model = AsignacionTurnoModel.builder()
                .choferId(chofer.getId())
                .choferNombre(chofer.getNombre())
                .unidadId(unidad.getId())
                .numeroPadron(unidad.getNumeroPadron())
                .placa(unidad.getPlaca())
                .lineaId(unidad.getLineaId())
                .lineaNombre(unidad.getLineaNombre())
                .subrutaId(request.getSubrutaId())
                .subrutaCodigo(request.getSubrutaCodigo())
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(request.getHoraFin())
                .estado("EN_RUTA")
                .porcentajeCumplimiento(BigDecimal.ZERO)
                .createdAt(OffsetDateTime.now())
                .build();

        try {
            AsignacionTurnoModel guardado = turnoRepositoryPort.save(model);
            return toDto(guardado);
        } catch (DataIntegrityViolationException e) {
            // Protección ante doble envío/carrera: índices únicos parciales en BD
            throw new IllegalStateException("El chofer o la unidad ya tienen un turno activo o programado.");
        }
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
                .subrutaId(model.getSubrutaId())
                .subrutaCodigo(model.getSubrutaCodigo())
                .subrutaNombre(model.getSubrutaNombre())
                .fecha(model.getFecha())
                .horaInicio(model.getHoraInicio())
                .horaFin(model.getHoraFin())
                .estado(model.getEstado())
                .porcentajeCumplimiento(model.getPorcentajeCumplimiento())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
