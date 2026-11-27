package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.ParticipanteConversacion;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import com.gowayki.nest.domain.repository.ChatNotificacionPort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Limpieza administrativa de grupos heredados que no cumplen el modelo actual
 * (títulos "system…", claves de ruta corruptas o ids indicados explícitamente).
 *
 * NO borra nada físicamente: aplica el mismo soft delete de 72 horas que ya
 * existe y luego reconcilia los turnos activos para recrear los grupos correctos.
 */
@Service
public class DepurarGruposObsoletosUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;
    private final SolicitarEliminacionGrupoUseCase solicitarEliminacionGrupoUseCase;
    private final ChatNotificacionPort chatNotificacionPort;
    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;
    private final SincronizarGrupoRutaUseCase sincronizarGrupoRutaUseCase;

    public DepurarGruposObsoletosUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                         EliminacionGrupoPort eliminacionGrupoPort,
                                         SolicitarEliminacionGrupoUseCase solicitarEliminacionGrupoUseCase,
                                         ChatNotificacionPort chatNotificacionPort,
                                         AsignacionTurnoRepositoryPort turnoRepositoryPort,
                                         SincronizarGrupoRutaUseCase sincronizarGrupoRutaUseCase) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.solicitarEliminacionGrupoUseCase = solicitarEliminacionGrupoUseCase;
        this.chatNotificacionPort = chatNotificacionPort;
        this.turnoRepositoryPort = turnoRepositoryPort;
        this.sincronizarGrupoRutaUseCase = sincronizarGrupoRutaUseCase;
    }

    @Transactional
    public Resultado ejecutar(List<UUID> ids, UUID administradorId) {
        if (administradorId == null) {
            throw new IllegalArgumentException("Se requiere un administrador autenticado.");
        }

        Set<UUID> objetivo = ids == null || ids.isEmpty()
                ? detectarObsoletos()
                : new LinkedHashSet<>(ids);

        List<UUID> marcados = new ArrayList<>();
        List<UUID> omitidos = new ArrayList<>();
        for (UUID id : objetivo) {
            if (id == null || !esGrupoVivo(id)) {
                omitidos.add(id);
                continue;
            }
            List<UUID> participantes = conversacionRepositoryPort.listarParticipantes(id).stream()
                    .map(ParticipanteConversacion::getUserId)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            solicitarEliminacionGrupoUseCase.ejecutarComoAdministrador(id, administradorId);
            chatNotificacionPort.grupoEliminado(id, participantes);
            marcados.add(id);
        }

        int sincronizados = 0;
        for (AsignacionTurnoModel turno : turnoRepositoryPort.findTurnosActivos()) {
            sincronizarGrupoRutaUseCase.paraTurno(turno);
            sincronizados++;
        }

        return new Resultado(marcados, omitidos, sincronizados);
    }

    private boolean esGrupoVivo(UUID id) {
        ConversacionModel c = conversacionRepositoryPort.findById(id).orElse(null);
        return c != null
                && Boolean.TRUE.equals(c.getEsGrupo())
                && c.getEliminadoEn() == null
                && eliminacionGrupoPort.buscarPendiente(id).isEmpty();
    }

    /**
     * Reinicio completo: marca TODOS los grupos vivos con el plazo de 72 horas
     * y luego reconcilia los turnos activos. Los grupos desaparecen ya de la UI
     * y se recrean solo los que corresponden a cada subruta real.
     */
    @Transactional
    public Resultado reiniciarTodo(UUID administradorId) {
        if (administradorId == null) {
            throw new IllegalArgumentException("Se requiere un administrador autenticado.");
        }
        List<UUID> todos = conversacionRepositoryPort.findAllGruposActivos().stream()
                .filter(c -> eliminacionGrupoPort.buscarPendiente(c.getId()).isEmpty())
                .map(com.gowayki.nest.domain.model.ConversacionModel::getId)
                .toList();
        return ejecutar(todos, administradorId);
    }

    /** Sin ids explícitos: títulos basura y claves de ruta que nunca fueron válidas. */
    private Set<UUID> detectarObsoletos() {
        Set<UUID> ids = new LinkedHashSet<>();
        for (ConversacionModel c : conversacionRepositoryPort.findAllGruposActivos()) {
            if (eliminacionGrupoPort.buscarPendiente(c.getId()).isPresent()) {
                continue;
            }
            String titulo = c.getTitulo();
            String clave = c.getClaveRuta();
            boolean tituloBasura = titulo != null
                    && titulo.trim().matches("(?i)^system\\b.*");
            boolean claveInvalida = clave != null
                    && (clave.isBlank()
                        || "GRUPO_RUTA".equalsIgnoreCase(clave.trim())
                        || clave.trim().matches("(?i)(l[ií]nea\\s+|canal\\s+de\\s+(l[ií]nea\\s+)?)?c\\d+")
                        || clave.trim().regionMatches(true, 0, "GRUPO ", 0, 6));
            if (tituloBasura || claveInvalida) {
                ids.add(c.getId());
            }
        }
        return ids;
    }

    public static class Resultado {
        private final List<UUID> marcados;
        private final List<UUID> omitidos;
        private final int turnosSincronizados;

        public Resultado(List<UUID> marcados, List<UUID> omitidos, int turnosSincronizados) {
            this.marcados = List.copyOf(marcados);
            this.omitidos = List.copyOf(omitidos);
            this.turnosSincronizados = turnosSincronizados;
        }

        public List<UUID> getMarcados() { return marcados; }
        public List<UUID> getOmitidos() { return omitidos; }
        public int getTurnosSincronizados() { return turnosSincronizados; }
    }
}
