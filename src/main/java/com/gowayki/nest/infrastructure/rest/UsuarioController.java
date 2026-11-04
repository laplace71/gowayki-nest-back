package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.UsuarioResponseDto;
import com.gowayki.nest.application.usecase.ObtenerUsuariosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final ObtenerUsuariosUseCase obtenerUsuariosUseCase;

    public UsuarioController(ObtenerUsuariosUseCase obtenerUsuariosUseCase) {
        this.obtenerUsuariosUseCase = obtenerUsuariosUseCase;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> listarTodos() {
        return ResponseEntity.ok(obtenerUsuariosUseCase.obtenerTodos());
    }
}