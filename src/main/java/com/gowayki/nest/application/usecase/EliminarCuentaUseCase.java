package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.model.ParticipanteConversacion;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import com.gowayki.nest.domain.repository.ArchivoPendienteBorradoPort;
import com.gowayki.nest.domain.repository.ChatNotificacionPort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * "Eliminar cuenta" sin borrar la fila de usuario:
 *  - sale de TODAS las conversaciones (también las que tienen purga pendiente);
 *  - cancela una purga si quien la pidió se va, para no borrar el grupo a los demás;
 *  - deja un aviso de sistema antes de irse;
 *  - transfiere administración si era el creador;
 *  - finaliza su turno activo y encola los audios de grupos que quedan vacíos;
 *  - marca el usuario inactivo y revoca sus tokens.
 */
@Service
public class EliminarCuentaUseCase {

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final MensajeSistemaService mensajeSistemaService;
    private final EliminacionGrupoPort eliminacionGrupoPort;
    private final ChatNotificacionPort chatNotificacionPort;
    private final ArchivoPendienteBorradoPort archivoPendienteBorradoPort;
    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;

    public EliminarCuentaUseCase(UsuarioRepositoryPort usuarioRepositoryPort,
                                 ConversacionRepositoryPort conversacionRepositoryPort,
                                 MensajeRepositoryPort mensajeRepositoryPort,
                                 MensajeSistemaService mensajeSistemaService,
                                 EliminacionGrupoPort eliminacionGrupoPort,
                                 ChatNotificacionPort chatNotificacionPort,
                                 ArchivoPendienteBorradoPort archivoPendienteBorradoPort,
                                 AsignacionTurnoRepositoryPort turnoRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.mensajeSistemaService = mensajeSistemaService;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.chatNotificacionPort = chatNotificacionPort;
        this.archivoPendienteBorradoPort = archivoPendienteBorradoPort;
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

        // 2) Todas las conversaciones, incluidas las que tienen purga pendiente.
        List<ConversacionModel> conversaciones =
                conversacionRepositoryPort.findByUsuarioIdIncluyendoPendientes(usuarioId);

        for (ConversacionModel c : conversaciones) {
            UUID conversacionId = c.getId();
            if (conversacionId == null) continue;

            boolean eraCreador = usuarioId.equals(c.getCreatedBy());
            List<ParticipanteConversacion> otros = conversacionRepositoryPort
                    .listarParticipantes(conversacionId).stream()
                    .filter(p -> p.getUserId() != null && !p.getUserId().equals(usuarioId))
                    .toList();

            // Si quien pidió la eliminación definitiva se va, se cancela: el
            // programador no debe borrar el grupo de quienes se quedan.
            var pendiente = eliminacionGrupoPort.buscarPendiente(conversacionId);
            if (pendiente.isPresent() && usuarioId.equals(pendiente.get().getSolicitadoPor())) {
                eliminacionGrupoPort.marcarCancelada(pendiente.get().getId(), usuarioId, ahora);
                conversacionRepositoryPort.desmarcarEliminado(conversacionId);
                mensajeRepositoryPort.desmarcarEliminados(conversacionId);
                chatNotificacionPort.eliminacionCancelada(
                        conversacionId,
                        otros.stream().map(ParticipanteConversacion::getUserId).toList()
                );
            }

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
                List<String> audios = mensajeRepositoryPort.findByConversacionId(conversacionId).stream()
                        .map(MensajeModel::getAudioUrl)
                        .filter(Objects::nonNull)
                        .toList();
                conversacionRepositoryPort.eliminar(conversacionId);
                audios.forEach(ruta -> archivoPendienteBorradoPort.encolar(ruta, "CUENTA_ELIMINADA"));
            }
        }

        // 3) Borrado lógico: conservamos id, nombre y mensajes; se revocan tokens.
        usuario.setTokenVersion((usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion()) + 1);
        usuario.setActivo(false);
        usuario.setIsOnline(false);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepositoryPort.save(usuario);
    }
}
