package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.AlertaSosResponseDto;
import com.gowayki.nest.application.dto.CrearAlertaSosRequestDto;
import com.gowayki.nest.domain.model.AlertaIncidenciaModel;
import com.gowayki.nest.domain.repository.AlertaIncidenciaRepositoryPort;
import com.gowayki.nest.domain.repository.UnidadRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CrearAlertaSosUseCase {

    private final AlertaIncidenciaRepositoryPort alertaIncidenciaRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final UnidadRepositoryPort unidadRepositoryPort;

    public CrearAlertaSosUseCase(AlertaIncidenciaRepositoryPort alertaIncidenciaRepositoryPort,
                                 UsuarioRepositoryPort usuarioRepositoryPort,
                                 UnidadRepositoryPort unidadRepositoryPort) {
        this.alertaIncidenciaRepositoryPort = alertaIncidenciaRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.unidadRepositoryPort = unidadRepositoryPort;
    }

    public AlertaSosResponseDto ejecutar(CrearAlertaSosRequestDto request) {
        if (request.getChoferId() == null) {
            throw new IllegalArgumentException("El chofer es obligatorio.");
        }
        if (request.getTipo() == null || request.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo es obligatorio.");
        }
        if (request.getOrigenDisparo() == null || request.getOrigenDisparo().isBlank()) {
            throw new IllegalArgumentException("El origen del disparo es obligatorio.");
        }
        if (request.getLatitud() == null || request.getLongitud() == null) {
            throw new IllegalArgumentException("La ubicacion es obligatoria.");
        }

        Optional<com.gowayki.nest.domain.model.UsuarioModel> usuarioOpt = usuarioRepositoryPort.findById(request.getChoferId());
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe usuario con ese id.");
        }

        if (request.getUnidadId() != null) {
            if (unidadRepositoryPort.findById(request.getUnidadId()).isEmpty()) {
                throw new IllegalArgumentException("No existe unidad con ese id.");
            }
        }

        OffsetDateTime ahora = OffsetDateTime.now();

        String tipo = request.getTipo().trim().toUpperCase();
        if (tipo.equals("SOS")) {
            tipo = "EMERGENCIA_SOS";
        }

        String origen = request.getOrigenDisparo().trim().toUpperCase();
        if (origen.equals("BOTON_FISICO") || origen.equals("BOTON_BLE")) {
            origen = "BOTON_BLE_FISICO";
        } else if (origen.equals("APP")) {
            origen = "APP_PANTALLA";
        }

        AlertaIncidenciaModel model = AlertaIncidenciaModel.builder()
                .choferId(request.getChoferId())
                .unidadId(request.getUnidadId())
                .tipo(tipo)
                .origenDisparo(origen)
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .descripcion(request.getDescripcion())
                .audioUrl(request.getAudioUrl())
                .estado("ACTIVA")
                .createdAt(ahora)
                .expiraAt(ahora.plusMinutes(30))
                .build();

        AlertaIncidenciaModel guardado = alertaIncidenciaRepositoryPort.save(model);

        return AlertaSosResponseDto.builder()
                .id(guardado.getId())
                .choferId(guardado.getChoferId())
                .unidadId(guardado.getUnidadId())
                .tipo(guardado.getTipo())
                .origenDisparo(guardado.getOrigenDisparo())
                .latitud(guardado.getLatitud())
                .longitud(guardado.getLongitud())
                .descripcion(guardado.getDescripcion())
                .audioUrl(guardado.getAudioUrl())
                .estado(guardado.getEstado())
                .createdAt(guardado.getCreatedAt())
                .expiraAt(guardado.getExpiraAt())
                .build();
    }
}