package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Un conductor solo puede unirse al grupo de ruta de la subruta que tiene
 * asignada en su turno activo. La identidad del chofer sale del JWT (la envía
 * el controller), nunca del cuerpo de la petición.
 */
@Service
public class ValidarSubrutaChoferUseCase {

    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;

    public ValidarSubrutaChoferUseCase(AsignacionTurnoRepositoryPort turnoRepositoryPort) {
        this.turnoRepositoryPort = turnoRepositoryPort;
    }

    public void ejecutar(UUID choferId, String claveRuta) {
        if (choferId == null || claveRuta == null || claveRuta.isBlank()) {
            return; // Otras validaciones ya rechazan la petición.
        }
        AsignacionTurnoModel turno = turnoRepositoryPort.findTurnoActivoPorChofer(choferId)
                .orElseThrow(() -> new AccesoDenegadoException(
                        "No tienes una subruta asignada para unirte a un grupo de ruta."));
        String asignada = turno.getSubrutaCodigo();
        if (asignada == null || !asignada.trim().equals(claveRuta.trim())) {
            throw new AccesoDenegadoException(
                    "Solo puedes unirte al grupo de ruta de tu subruta asignada.");
        }
    }
}
