package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.AuthResponseDto;
import com.gowayki.nest.application.dto.CambiarPinRequestDto;
import com.gowayki.nest.application.dto.LoginRequestDto;
import com.gowayki.nest.application.dto.RegistroSolicitudRequestDto;
import com.gowayki.nest.application.dto.RegistroUsuarioRequestDto;
import com.gowayki.nest.application.usecase.CambiarPinUseCase;
import com.gowayki.nest.application.usecase.CrearSolicitudesUseCase;
import com.gowayki.nest.application.usecase.EliminarCuentaUseCase;
import com.gowayki.nest.application.usecase.LoginUseCase;
import com.gowayki.nest.application.usecase.RegistroUsuarioUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegistroUsuarioUseCase registroUsuarioUseCase;
    private final EliminarCuentaUseCase eliminarCuentaUseCase;
    private final CambiarPinUseCase cambiarPinUseCase;
    private final CrearSolicitudesUseCase crearSolicitudesUseCase;

    public AuthController(LoginUseCase loginUseCase, RegistroUsuarioUseCase registroUsuarioUseCase,
                          EliminarCuentaUseCase eliminarCuentaUseCase,
                          CambiarPinUseCase cambiarPinUseCase,
                          CrearSolicitudesUseCase crearSolicitudesUseCase) {
        this.loginUseCase = loginUseCase;
        this.registroUsuarioUseCase = registroUsuarioUseCase;
        this.eliminarCuentaUseCase = eliminarCuentaUseCase;
        this.cambiarPinUseCase = cambiarPinUseCase;
        this.crearSolicitudesUseCase = crearSolicitudesUseCase;
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

    /** Borrado lógico: sale de los grupos y desactiva la cuenta sin borrar mensajes. */
    @PostMapping("/eliminar-cuenta")
    public ResponseEntity<?> eliminarCuenta(Authentication autenticacion) {
        try {
            if (autenticacion == null || autenticacion.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "exito", false,
                        "mensaje", "Se requiere un token válido."
                ));
            }
            eliminarCuentaUseCase.ejecutar(UUID.fromString(autenticacion.getPrincipal().toString()));
            return ResponseEntity.ok(Map.of(
                    "exito", true,
                    "mensaje", "Cuenta eliminada. Puedes volver a registrarte con tu DNI."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "exito", false,
                    "mensaje", "No se pudo eliminar la cuenta: " + e.getMessage()
            ));
        }
    }

    /** Cambia el PIN de la cuenta autenticada. Exige el PIN actual. */
    @PostMapping("/cambiar-pin")
    public ResponseEntity<?> cambiarPin(@RequestBody CambiarPinRequestDto request,
                                        Authentication autenticacion) {
        try {
            if (autenticacion == null || autenticacion.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "exito", false, "mensaje", "Se requiere un token válido."));
            }
            cambiarPinUseCase.ejecutar(
                    UUID.fromString(autenticacion.getPrincipal().toString()),
                    request == null ? null : request.getPinActual(),
                    request == null ? null : request.getPinNuevo()
            );
            return ResponseEntity.ok(Map.of("exito", true, "mensaje", "PIN actualizado correctamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "exito", false, "mensaje", "No se pudo cambiar el PIN."));
        }
    }

    /** La app crea una solicitud de chofer; el jefe la aprueba en el panel. */
    @PostMapping("/registro-solicitud")
    public ResponseEntity<?> registroSolicitud(@RequestBody RegistroSolicitudRequestDto request) {
        try {
            var solicitud = crearSolicitudesUseCase.registroChofer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "exito", true,
                    "solicitudId", solicitud.getId(),
                    "mensaje", "Solicitud enviada. El jefe de ruta debe aprobar tu acceso."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "exito", false, "mensaje", "No se pudo enviar la solicitud."));
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

