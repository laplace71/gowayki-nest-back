package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.AsignarTurnoRequestDto;
import com.gowayki.nest.application.dto.TurnoResponseDto;
import com.gowayki.nest.application.usecase.AsignarTurnoUseCase;
import com.gowayki.nest.application.usecase.CambiarSubrutaTurnoUseCase;
import com.gowayki.nest.application.usecase.FinalizarTurnoUseCase;
import com.gowayki.nest.application.usecase.IniciarTurnoUseCase;
import com.gowayki.nest.application.usecase.ObtenerTurnoActivoUseCase;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/turnos")
@CrossOrigin(origins = "*")
public class TurnoController {

    private final AsignarTurnoUseCase asignarTurnoUseCase;
    private final IniciarTurnoUseCase iniciarTurnoUseCase;
    private final FinalizarTurnoUseCase finalizarTurnoUseCase;
    private final ObtenerTurnoActivoUseCase obtenerTurnoActivoUseCase;
    private final CambiarSubrutaTurnoUseCase cambiarSubrutaTurnoUseCase;

    public TurnoController(AsignarTurnoUseCase asignarTurnoUseCase,
                           IniciarTurnoUseCase iniciarTurnoUseCase,
                           FinalizarTurnoUseCase finalizarTurnoUseCase,
                           ObtenerTurnoActivoUseCase obtenerTurnoActivoUseCase,
                           CambiarSubrutaTurnoUseCase cambiarSubrutaTurnoUseCase) {
        this.asignarTurnoUseCase = asignarTurnoUseCase;
        this.iniciarTurnoUseCase = iniciarTurnoUseCase;
        this.finalizarTurnoUseCase = finalizarTurnoUseCase;
        this.obtenerTurnoActivoUseCase = obtenerTurnoActivoUseCase;
        this.cambiarSubrutaTurnoUseCase = cambiarSubrutaTurnoUseCase;
    }

    /** Cambia la subruta del turno activo sin cerrarlo ni crear otro. */
    @PutMapping("/{id}/subruta")
    public ResponseEntity<?> cambiarSubruta(@PathVariable UUID id,
                                            @RequestBody Map<String, String> body,
                                            Authentication autenticacion) {
        try {
            if (autenticacion == null || autenticacion.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("exito", false, "mensaje", "Se requiere un token válido."));
            }
            UUID solicitante = UUID.fromString(autenticacion.getPrincipal().toString());
            boolean administrador = autenticacion.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_JEFE_RUTA".equals(a.getAuthority()));
            return ResponseEntity.ok(
                cambiarSubrutaTurnoUseCase.ejecutar(id, body.get("subrutaCodigo"), solicitante, administrador));
        } catch (AccesoDenegadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("exito", false, "mensaje", "Error interno al cambiar subruta: " + e.getMessage()));
        }
    }

    @PostMapping("/asignar")
    public ResponseEntity<?> asignar(@RequestBody AsignarTurnoRequestDto request) {
        try {
            TurnoResponseDto response = asignarTurnoUseCase.ejecutar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error interno al asignar turno: " + e.getMessage()));
        }
    }

    @GetMapping("/chofer/{choferId}/activo")
    public ResponseEntity<?> obtenerTurnoActivo(@PathVariable UUID choferId) {
        return obtenerTurnoActivoUseCase.obtenerPorChofer(choferId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<?> iniciar(@PathVariable UUID id) {
        try {
            TurnoResponseDto response = iniciarTurnoUseCase.ejecutar(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error interno al iniciar turno: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable UUID id) {
        try {
            TurnoResponseDto response = finalizarTurnoUseCase.ejecutar(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error interno al finalizar turno: " + e.getMessage()));
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<List<TurnoResponseDto>> listarActivos() {
        return ResponseEntity.ok(obtenerTurnoActivoUseCase.listarTodosActivos());
    }
}
