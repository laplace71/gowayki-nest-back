package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.PuntoControlDto;
import com.gowayki.nest.application.usecase.ObtenerPuntosControlUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lineas")
@CrossOrigin(origins = "*")
public class PuntoControlController {

    private final ObtenerPuntosControlUseCase obtenerPuntosControlUseCase;

    public PuntoControlController(ObtenerPuntosControlUseCase obtenerPuntosControlUseCase) {
        this.obtenerPuntosControlUseCase = obtenerPuntosControlUseCase;
    }

    @GetMapping("/{lineaId}/puntos-control")
    public ResponseEntity<List<PuntoControlDto>> listarPorLinea(@PathVariable Integer lineaId) {
        return ResponseEntity.ok(obtenerPuntosControlUseCase.obtenerPorLinea(lineaId));
    }
}