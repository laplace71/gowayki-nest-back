package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.MensajeResponseDto;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.AudioStoragePort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class EnviarMensajeAudioUseCase {

    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final AudioStoragePort audioStoragePort;

    public EnviarMensajeAudioUseCase(MensajeRepositoryPort mensajeRepositoryPort,
                                    ConversacionRepositoryPort conversacionRepositoryPort,
                                    UsuarioRepositoryPort usuarioRepositoryPort,
                                    AudioStoragePort audioStoragePort) {
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.audioStoragePort = audioStoragePort;
    }

    public MensajeResponseDto ejecutar(UUID conversacionId, UUID emisorId, byte[] audioBytes, String extensionOriginal, Integer duracionSegundos) {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalArgumentException("El archivo de audio no puede estar vacío.");
        }

        ConversacionModel conversacion = conversacionRepositoryPort.findById(conversacionId)
                .orElseThrow(() -> new IllegalArgumentException("La conversación no existe."));

        UsuarioModel emisor = usuarioRepositoryPort.findById(emisorId)
                .orElseThrow(() -> new IllegalArgumentException("El emisor no existe."));

        String audioUrl = audioStoragePort.guardarAudio(audioBytes, extensionOriginal);

        MensajeModel model = MensajeModel.builder()
                .conversacionId(conversacion.getId())
                .emisorId(emisor.getId())
                .emisorNombre(emisor.getNombre())
                .tipo("AUDIO")
                .contenidoTexto(null)
                .audioUrl(audioUrl)
                .audioDuracionSegundos(duracionSegundos != null ? duracionSegundos : 0)
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
