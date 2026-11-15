package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.PosicionGpsDto;
import com.gowayki.nest.application.dto.UnidadResponseDto;
import com.gowayki.nest.application.dto.ActualizarPosicionGpsRequestDto;
import com.gowayki.nest.application.usecase.ActualizarPosicionGpsUseCase;
import com.gowayki.nest.application.usecase.ObtenerUnidadesUseCase;
import com.gowayki.nest.infrastructure.websocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UnidadController {

    private final ObtenerUnidadesUseCase obtenerUnidadesUseCase;
    private final ActualizarPosicionGpsUseCase actualizarPosicionGpsUseCase;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public UnidadController(ObtenerUnidadesUseCase obtenerUnidadesUseCase,
                            ActualizarPosicionGpsUseCase actualizarPosicionGpsUseCase,
                            ChatWebSocketHandler chatWebSocketHandler) {
        this.obtenerUnidadesUseCase = obtenerUnidadesUseCase;
        this.actualizarPosicionGpsUseCase = actualizarPosicionGpsUseCase;
        this.chatWebSocketHandler = chatWebSocketHandler;
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
        PosicionGpsDto response = actualizarPosicionGpsUseCase.ejecutar(id, request);
        chatWebSocketHandler.emitirMensaje(response);
        return ResponseEntity.ok(response);
    }
}

