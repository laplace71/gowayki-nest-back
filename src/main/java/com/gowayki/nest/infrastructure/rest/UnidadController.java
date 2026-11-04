package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.PosicionGpsDto;
import com.gowayki.nest.application.dto.UnidadResponseDto;
import com.gowayki.nest.application.dto.ActualizarPosicionGpsRequestDto;
import com.gowayki.nest.application.usecase.ActualizarPosicionGpsUseCase;
import com.gowayki.nest.application.usecase.ObtenerUnidadesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UnidadController {

    private final ObtenerUnidadesUseCase obtenerUnidadesUseCase;
    private final ActualizarPosicionGpsUseCase actualizarPosicionGpsUseCase;

    public UnidadController(ObtenerUnidadesUseCase obtenerUnidadesUseCase,
                            ActualizarPosicionGpsUseCase actualizarPosicionGpsUseCase) {
        this.obtenerUnidadesUseCase = obtenerUnidadesUseCase;
        this.actualizarPosicionGpsUseCase = actualizarPosicionGpsUseCase;
    }

    @GetMapping("/unidades")
    public ResponseEntity<List<UnidadResponseDto>> listarTodas() {
        return ResponseEntity.ok(obtenerUnidadesUseCase.obtenerTodas());
    }

    @GetMapping("/lineas/{lineaId}/unidades")
    public ResponseEntity<List<UnidadResponseDto>> listarPorLinea(@PathVariable Integer lineaId) {
        return ResponseEntity.ok(obtenerUnidadesUseCase.obtenerPorLinea(lineaId));
    }

    @GetMapping("/unidades/{id}")
    public ResponseEntity<UnidadResponseDto> obtenerPorId(@PathVariable Integer id) {
        return obtenerUnidadesUseCase.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/unidades/{id}/posicion")
    public ResponseEntity<PosicionGpsDto> actualizarPosicion(@PathVariable Integer id,
                                                             @RequestBody ActualizarPosicionGpsRequestDto request) {
        return ResponseEntity.ok(actualizarPosicionGpsUseCase.ejecutar(id, request));
    }
}

