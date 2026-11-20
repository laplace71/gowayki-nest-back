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

    /** Aviso personal: ese usuario limpió el chat en otro dispositivo. */
    void conversacionLimpiada(UUID conversacionId, UUID usuarioId, OffsetDateTime limpiadoEn);

    /** Aviso personal: ese usuario ocultó un mensaje en otro dispositivo. */
    void mensajeOculto(UUID conversacionId, UUID mensajeId, UUID usuarioId);

    /** Aviso a todos: el emisor eliminó un mensaje para todos. */
    void mensajeEliminado(UUID conversacionId, UUID mensajeId, List<UUID> participantes);
}
