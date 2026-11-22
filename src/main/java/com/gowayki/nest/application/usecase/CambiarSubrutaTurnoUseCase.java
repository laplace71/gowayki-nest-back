package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Cambia la subruta de un turno ya activo (sin crear otro turno). */
@Service
public class CambiarSubrutaTurnoUseCase {

    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;

    public CambiarSubrutaTurnoUseCase(AsignacionTurnoRepositoryPort turnoRepositoryPort) {
        this.turnoRepositoryPort = turnoRepositoryPort;
    }

    public AsignacionTurnoModel ejecutar(UUID turnoId, String subrutaCodigo) {
        if (turnoId == null) {
            throw new IllegalArgumentException("El id del turno es obligatorio.");
        }
        if (subrutaCodigo == null || subrutaCodigo.isBlank()) {
            throw new IllegalArgumentException("El código de subruta es obligatorio.");
        }
        return turnoRepositoryPort.cambiarSubruta(turnoId, subrutaCodigo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado."));
    }
}
