package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.LineaResponseDto;
import com.gowayki.nest.application.usecase.ObtenerLineasUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lineas")
@CrossOrigin(origins = "*") // Permite llamadas desde app móvil o web
public class LineaController {

    private final ObtenerLineasUseCase obtenerLineasUseCase;

    public LineaController(ObtenerLineasUseCase obtenerLineasUseCase) {
        this.obtenerLineasUseCase = obtenerLineasUseCase;
    }

    @GetMapping
    public ResponseEntity<List<LineaResponseDto>> listarLineas() {
        List<LineaResponseDto> lineas = obtenerLineasUseCase.ejecutar();
        return ResponseEntity.ok(lineas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LineaResponseDto> obtenerPorId(@PathVariable Integer id) {
        return obtenerLineasUseCase.ejecutarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

