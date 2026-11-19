package com.gowayki.nest.domain.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Avisos del ciclo de vida de un grupo. La implementación real es WebSocket;
 * el caso de uso no necesita saberlo.
 */
public interface ChatNotificacionPort {

    void eliminacionPendiente(UUID conversacionId, String titulo, OffsetDateTime ejecutarDespues,
                              List<UUID> participantes);

    void eliminacionCancelada(UUID conversacionId, List<UUID> participantes);

    void grupoEliminado(UUID conversacionId, List<UUID> participantes);
}
