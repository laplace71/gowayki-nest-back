package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
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

    public AsignacionTurnoModel ejecutar(UUID turnoId, String subrutaCodigo,
                                         UUID solicitanteId, boolean administrador) {
        if (solicitanteId == null) {
            throw new NoAutenticadoException("Se requiere un token válido.");
        }
        if (turnoId == null) {
            throw new IllegalArgumentException("El id del turno es obligatorio.");
        }
        if (subrutaCodigo == null || subrutaCodigo.isBlank()) {
            throw new IllegalArgumentException("El código de subruta es obligatorio.");
        }

        AsignacionTurnoModel turno = turnoRepositoryPort.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado."));
        if (!administrador && (turno.getChoferId() == null
                || !turno.getChoferId().equals(solicitanteId))) {
            throw new AccesoDenegadoException("No puedes cambiar la ruta de otro chofer.");
        }
        if (!"EN_RUTA".equals(turno.getEstado()) && !"PROGRAMADO".equals(turno.getEstado())) {
            throw new IllegalArgumentException("El turno ya no está activo.");
        }

        return turnoRepositoryPort.cambiarSubruta(turnoId, subrutaCodigo.trim())
                .orElseThrow(() -> new IllegalArgumentException("El turno ya no está activo."));
    }
}
