package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.AlertaSosResponseDto;
import com.gowayki.nest.application.dto.CrearAlertaSosRequestDto;
import com.gowayki.nest.application.usecase.CrearAlertaSosUseCase;
import com.gowayki.nest.application.usecase.ObtenerAlertasSosUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sos")
@CrossOrigin(origins = "*")
public class SosController {

    private final CrearAlertaSosUseCase crearAlertaSosUseCase;
    private final ObtenerAlertasSosUseCase obtenerAlertasSosUseCase;

    public SosController(CrearAlertaSosUseCase crearAlertaSosUseCase,
                         ObtenerAlertasSosUseCase obtenerAlertasSosUseCase) {
        this.crearAlertaSosUseCase = crearAlertaSosUseCase;
        this.obtenerAlertasSosUseCase = obtenerAlertasSosUseCase;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearAlertaSosRequestDto request) {
        try {
            AlertaSosResponseDto response = crearAlertaSosUseCase.ejecutar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exito", false,
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "exito", false,
                    "mensaje", "Error interno al crear la alerta SOS: " + e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<List<AlertaSosResponseDto>> listarTodas() {
        return ResponseEntity.ok(obtenerAlertasSosUseCase.listarTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<AlertaSosResponseDto>> listarActivas() {
        return ResponseEntity.ok(obtenerAlertasSosUseCase.listarActivas());
    }
}