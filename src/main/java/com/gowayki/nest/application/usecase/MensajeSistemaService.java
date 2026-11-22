package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Avisos de sistema dentro de una conversación ("salió del grupo", "se unió").
 * Se guardan como mensajes de tipo SISTEMA con fecha de ahora, de modo que
 * respetan el corte por agregadoEn: quien vuelve a entrar ve el aviso nuevo
 * pero no el historial anterior a su salida.
 */
@Service
public class MensajeSistemaService {

    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public MensajeSistemaService(MensajeRepositoryPort mensajeRepositoryPort,
                                 UsuarioRepositoryPort usuarioRepositoryPort) {
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public void avisar(UUID conversacionId, UUID usuarioId, String accion) {
        if (conversacionId == null || usuarioId == null) return;
        String nombre = usuarioRepositoryPort.findById(usuarioId)
                .map(u -> u.getNombre())
                .orElse("Un conductor");
        mensajeRepositoryPort.save(MensajeModel.builder()
                .conversacionId(conversacionId)
                .emisorId(usuarioId)
                .emisorNombre(nombre)
                .tipo("SISTEMA")
                .contenidoTexto(nombre + " " + accion)
                .estado("ENVIADO")
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
