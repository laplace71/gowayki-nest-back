package com.gowayki.nest.infrastructure.config;

import com.gowayki.nest.application.usecase.DepurarGruposObsoletosUseCase;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Depuración ONE-SHOT de grupos heredados.
 *
 * Se activa SOLO con:
 *   -Dapp.depurar-grupos.habilitado=true
 *
 * Opcionalmente:
 *   -Dapp.depurar-grupos.ids=uuid1,uuid2
 *   -Dapp.depurar-grupos.admin-id=<uuid>
 *
 * Si no se dan ids, detecta automáticamente títulos "system…" y claves de ruta
 * inválidas. Si no se da admin-id, usa el primer usuario con rol ADMIN.
 */
@Component
public class DepuracionGruposRunner implements CommandLineRunner {

    private final DepurarGruposObsoletosUseCase depurarGruposObsoletosUseCase;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Value("${app.depurar-grupos.habilitado:false}")
    private boolean habilitado;

    @Value("${app.depurar-grupos.ids:}")
    private String ids;

    @Value("${app.depurar-grupos.admin-id:}")
    private String adminId;

    @Value("${app.reiniciar-grupos.habilitado:false}")
    private boolean reiniciar;

    public DepuracionGruposRunner(DepurarGruposObsoletosUseCase depurarGruposObsoletosUseCase,
                                  UsuarioRepositoryPort usuarioRepositoryPort) {
        this.depurarGruposObsoletosUseCase = depurarGruposObsoletosUseCase;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public void run(String... args) {
        if (!habilitado && !reiniciar) {
            return;
        }
        UUID administrador = resolverAdministrador();
        if (administrador == null) {
            System.out.println(">>> [Depuración] No hay usuario ADMIN; no se marcó ningún grupo.");
            return;
        }

        DepurarGruposObsoletosUseCase.Resultado resultado;
        if (reiniciar) {
            resultado = depurarGruposObsoletosUseCase.reiniciarTodo(administrador);
        } else {
            List<UUID> objetivo = ids == null || ids.isBlank()
                    ? null
                    : java.util.Arrays.stream(ids.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(UUID::fromString)
                            .toList();
            resultado = depurarGruposObsoletosUseCase.ejecutar(objetivo, administrador);
        }
        System.out.println(">>> [Depuración] grupos marcados para 72 h: " + resultado.getMarcados().size()
                + ", omitidos: " + resultado.getOmitidos().size()
                + ", turnos sincronizados: " + resultado.getTurnosSincronizados());
    }

    private UUID resolverAdministrador() {
        if (adminId != null && !adminId.isBlank()) {
            return UUID.fromString(adminId.trim());
        }
        return usuarioRepositoryPort.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRol()))
                .map(UsuarioModel::getId)
                .findFirst()
                .orElse(null);
    }
}
