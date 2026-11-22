package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.ParticipanteConversacion;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * "Eliminar cuenta" sin borrar la fila de usuario:
 *  - sale de TODAS las conversaciones (el grupo sigue para los demás);
 *  - deja un aviso de sistema antes de irse;
 *  - transfiere administración si era el creador;
 *  - finaliza su turno activo;
 *  - marca el usuario inactivo (la fila y sus mensajes se conservan).
 *
 * Los mensajes que envió siguen en el historial de los demás con su nombre,
 * porque emisorNombre se resuelve desde esta misma fila de usuario.
 */
@Service
public class EliminarCuentaUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeSistemaService mensajeSistemaService;
    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;

    public EliminarCuentaUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                 ConversacionRepositoryPort conversacionRepositoryPort,
                                 MensajeSistemaService mensajeSistemaService,
                                 AsignacionTurnoRepositoryPort turnoRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeSistemaService = mensajeSistemaService;
        this.turnoRepositoryPort = turnoRepositoryPort;
    }

    @Transactional
    public void ejecutar(UUID usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Se requiere un usuario autenticado.");
        }
        UsuarioModel usuario = usuarioRepositoryPort.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        String nombre = usuario.getNombre();
        OffsetDateTime ahora = OffsetDateTime.now();

        // 1) Turno activo: se cierra para liberar la unidad.
        java.util.Optional<AsignacionTurnoModel> turno =
                turnoRepositoryPort.findTurnoActivoPorChofer(usuarioId);
        if (turno.isPresent()) {
            AsignacionTurnoModel t = turno.get();
            t.setEstado("FINALIZADO");
            t.setHoraFin(ahora);
            turnoRepositoryPort.save(t);
        }

        // 2) Todas las conversaciones en las que participa.
        List<ConversacionModel> conversaciones =
                conversacionRepositoryPort.findByUsuarioIdSinFiltroOcultas(usuarioId);

        for (ConversacionModel c : conversaciones) {
            UUID conversacionId = c.getId();
            if (conversacionId == null) continue;

            boolean eraCreador = usuarioId.equals(c.getCreatedBy());
            List<ParticipanteConversacion> otros = conversacionRepositoryPort
                    .listarParticipantes(conversacionId).stream()
                    .filter(p -> p.getUserId() != null && !p.getUserId().equals(usuarioId))
                    .toList();

            // Aviso antes de salir: los que se quedan lo ven; él ya no.
            mensajeSistemaService.avisar(conversacionId, usuarioId, "salió del grupo");

            boolean grupo = Boolean.TRUE.equals(c.getEsGrupo())
                    || "GRUPO_RUTA".equals(c.getTipoConversacion());

            if (eraCreador && !otros.isEmpty() && grupo) {
                UUID heredero = otros.get(0).getUserId();
                conversacionRepositoryPort.cambiarRol(conversacionId, heredero, "ADMIN");
                conversacionRepositoryPort.cambiarCreador(conversacionId, heredero);
            }

            conversacionRepositoryPort.quitarParticipante(conversacionId, usuarioId);
            conversacionRepositoryPort.limpiarEstadoUsuario(conversacionId, usuarioId);
            if (grupo) {
                conversacionRepositoryPort.promoverAdministradorSiHaceFalta(conversacionId);
            }

            if (conversacionRepositoryPort.contarParticipantes(conversacionId) == 0) {
                conversacionRepositoryPort.eliminar(conversacionId);
            }
        }

        // 3) Borrado lógico: conservamos id, nombre y mensajes.
        // La versión cambia para revocar de inmediato los JWT ya emitidos.
        usuario.setTokenVersion((usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion()) + 1);
        usuario.setActivo(false);
        usuario.setIsOnline(false);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepositoryPort.save(usuario);
    }
}
