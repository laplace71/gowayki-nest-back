package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.AuthResponseDto;
import com.gowayki.nest.application.dto.LoginRequestDto;
import com.gowayki.nest.application.dto.RegistroUsuarioRequestDto;
import com.gowayki.nest.application.usecase.LoginUseCase;
import com.gowayki.nest.application.usecase.RegistroUsuarioUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegistroUsuarioUseCase registroUsuarioUseCase;

    public AuthController(LoginUseCase loginUseCase, RegistroUsuarioUseCase registroUsuarioUseCase) {
        this.loginUseCase = loginUseCase;
        this.registroUsuarioUseCase = registroUsuarioUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            AuthResponseDto response = loginUseCase.ejecutar(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exito", false,
                    "mensaje", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                    "exito", false,
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "exito", false,
                    "mensaje", "Error interno al procesar el inicio de sesión: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroUsuarioRequestDto request) {
        try {
            AuthResponseDto response = registroUsuarioUseCase.ejecutar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exito", false,
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "exito", false,
                    "mensaje", "Error interno al registrar el usuario: " + e.getMessage()
            ));
        }
    }
}

