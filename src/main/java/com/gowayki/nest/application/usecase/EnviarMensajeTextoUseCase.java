package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.EnviarMensajeTextoRequestDto;
import com.gowayki.nest.application.dto.MensajeResponseDto;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class EnviarMensajeTextoUseCase {

    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public EnviarMensajeTextoUseCase(MensajeRepositoryPort mensajeRepositoryPort,
                                    ConversacionRepositoryPort conversacionRepositoryPort,
                                    UsuarioRepositoryPort usuarioRepositoryPort) {
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public MensajeResponseDto ejecutar(UUID conversacionId, EnviarMensajeTextoRequestDto request) {
        if (request.getContenidoTexto() == null || request.getContenidoTexto().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido del mensaje no puede estar vacío.");
        }

        ConversacionModel conversacion = conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        UsuarioModel emisor = usuarioRepositoryPort.findById(request.getEmisorId())
                .orElseThrow(() -> new IllegalArgumentException("El emisor no existe."));

        MensajeModel model = MensajeModel.builder()
                .conversacionId(conversacion.getId())
                .emisorId(emisor.getId())
                .emisorNombre(emisor.getNombre())
                .tipo("TEXTO")
                .contenidoTexto(request.getContenidoTexto().trim())
                .audioUrl(null)
                .audioDuracionSegundos(0)
                .estado("ENVIADO")
                .createdAt(OffsetDateTime.now())
                .build();

        MensajeModel guardado = mensajeRepositoryPort.save(model);

        return MensajeResponseDto.builder()
                .id(guardado.getId())
                .conversacionId(guardado.getConversacionId())
                .emisorId(guardado.getEmisorId())
                .emisorNombre(guardado.getEmisorNombre())
                .tipo(guardado.getTipo())
                .contenidoTexto(guardado.getContenidoTexto())
                .audioUrl(guardado.getAudioUrl())
                .audioDuracionSegundos(guardado.getAudioDuracionSegundos())
                .estado(guardado.getEstado())
                .createdAt(guardado.getCreatedAt())
                .build();
    }
}
