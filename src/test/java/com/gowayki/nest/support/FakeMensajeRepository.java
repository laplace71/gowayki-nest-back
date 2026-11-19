package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Mensajes en memoria, suficientes para audios y conteos. */
public class FakeMensajeRepository implements MensajeRepositoryPort {

    public final List<MensajeModel> mensajes = new ArrayList<>();
    public final Set<String> audiosExistentes = new LinkedHashSet<>();
    public final List<String> llamadas = new ArrayList<>();

    public void agregarAudio(UUID conversacionId, UUID emisorId, String audioUrl) {
        mensajes.add(MensajeModel.builder()
                .id(UUID.randomUUID())
                .conversacionId(conversacionId)
                .emisorId(emisorId)
                .tipo("AUDIO")
                .audioUrl(audioUrl)
                .audioDuracionSegundos(3)
                .build());
        audiosExistentes.add(audioUrl);
    }

    @Override
    public MensajeModel save(MensajeModel model) {
        if (model.getId() == null) {
            model.setId(UUID.randomUUID());
        }
        mensajes.add(model);
        if (model.getAudioUrl() != null) {
            audiosExistentes.add(model.getAudioUrl());
        }
        return model;
    }

    @Override
    public List<MensajeModel> findByConversacionId(UUID conversacionId) {
        return mensajes.stream()
                .filter(m -> conversacionId.equals(m.getConversacionId()))
                .toList();
    }

    @Override
    public Optional<MensajeModel> findUltimoMensaje(UUID conversacionId) {
        return findByConversacionId(conversacionId).stream().reduce((a, b) -> b);
    }

    @Override
    public int contarMensajes(UUID conversacionId) {
        return findByConversacionId(conversacionId).size();
    }

    @Override
    public boolean existeAudioUrl(String audioUrl) {
        return audioUrl != null && audiosExistentes.contains(audioUrl);
    }
}
