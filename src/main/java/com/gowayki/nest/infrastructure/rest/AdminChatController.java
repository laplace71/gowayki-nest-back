package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.DepurarGruposRequestDto;
import com.gowayki.nest.application.usecase.DepurarGruposObsoletosUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/** Operaciones administrativas de Comunicación. Requiere ROLE_ADMIN. */
@RestController
@RequestMapping("/api/admin/chat")
@CrossOrigin(origins = "*")
public class AdminChatController {

    private final DepurarGruposObsoletosUseCase depurarGruposObsoletosUseCase;

    public AdminChatController(DepurarGruposObsoletosUseCase depurarGruposObsoletosUseCase) {
        this.depurarGruposObsoletosUseCase = depurarGruposObsoletosUseCase;
    }

    /**
     * Marca los grupos obsoletos con el plazo de 72 horas y sincroniza los
     * turnos activos. Con cuerpo vacío detecta automáticamente los obsoletos.
     */
    /** Reinicia TODOS los grupos: los marca para 72 h y recrea los correctos. */
    @PostMapping("/reiniciar-grupos")
    public ResponseEntity<?> reiniciar(Authentication autenticacion) {
        if (autenticacion == null || autenticacion.getPrincipal() == null
                || !autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("exito", false,
                    "mensaje", "Solo un administrador puede reiniciar los grupos."));
        }
        UUID adminId = UUID.fromString(autenticacion.getPrincipal().toString());
        DepurarGruposObsoletosUseCase.Resultado resultado =
                depurarGruposObsoletosUseCase.reiniciarTodo(adminId);
        return ResponseEntity.ok(Map.of(
                "exito", true,
                "marcados", resultado.getMarcados(),
                "omitidos", resultado.getOmitidos(),
                "turnosSincronizados", resultado.getTurnosSincronizados()
        ));
    }

    @PostMapping("/depurar-grupos")
    public ResponseEntity<?> depurar(@RequestBody(required = false) DepurarGruposRequestDto request,
                                     Authentication autenticacion) {
        if (autenticacion == null || autenticacion.getPrincipal() == null
                || !autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("exito", false,
                    "mensaje", "Solo un administrador puede depurar los grupos."));
        }

        UUID adminId = UUID.fromString(autenticacion.getPrincipal().toString());
        DepurarGruposObsoletosUseCase.Resultado resultado =
                depurarGruposObsoletosUseCase.ejecutar(
                        request == null ? null : request.getConversacionIds(), adminId);

        return ResponseEntity.ok(Map.of(
                "exito", true,
                "marcados", resultado.getMarcados(),
                "omitidos", resultado.getOmitidos(),
                "turnosSincronizados", resultado.getTurnosSincronizados()
        ));
    }
}
