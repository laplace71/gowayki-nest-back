package com.gowayki.nest.support;

import com.gowayki.nest.domain.repository.ChatNotificacionPort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Guarda las notificaciones para comprobar a quién se avisó. */
public class FakeChatNotificacion implements ChatNotificacionPort {

    public final List<UUID> conversacionesConPendiente = new ArrayList<>();
    public final List<UUID> conversacionesCanceladas = new ArrayList<>();
    public final List<UUID> conversacionesEliminadas = new ArrayList<>();
    public final List<List<UUID>> destinatarios = new ArrayList<>();

    @Override
    public void eliminacionPendiente(UUID conversacionId, String titulo, OffsetDateTime ejecutarDespues,
                                     List<UUID> participantes) {
        conversacionesConPendiente.add(conversacionId);
        destinatarios.add(List.copyOf(participantes));
    }

    @Override
    public void eliminacionCancelada(UUID conversacionId, List<UUID> participantes) {
        conversacionesCanceladas.add(conversacionId);
        destinatarios.add(List.copyOf(participantes));
    }

    @Override
    public void grupoEliminado(UUID conversacionId, List<UUID> participantes) {
        conversacionesEliminadas.add(conversacionId);
        destinatarios.add(List.copyOf(participantes));
    }
}
