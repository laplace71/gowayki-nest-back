package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Total de no leídos del usuario, para actualizar el badge fuera del módulo. */
@Service
public class ObtenerTotalNoLeidosUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;

    public ObtenerTotalNoLeidosUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                       MensajeRepositoryPort mensajeRepositoryPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
    }

    public int ejecutar(UUID usuarioId) {
        return conversacionRepositoryPort.findByUsuarioId(usuarioId).stream()
                .mapToInt(c -> mensajeRepositoryPort.contarNoLeidos(c.getId(), usuarioId))
                .sum();
    }
}
