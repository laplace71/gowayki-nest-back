package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.UnidadResponseDto;
import com.gowayki.nest.application.usecase.ObtenerUnidadesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UnidadController {

    private final ObtenerUnidadesUseCase obtenerUnidadesUseCase;

    public UnidadController(ObtenerUnidadesUseCase obtenerUnidadesUseCase) {
        this.obtenerUnidadesUseCase = obtenerUnidadesUseCase;
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
}

