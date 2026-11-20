package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Mensajes en memoria, suficientes para audios y conteos. */
public class FakeMensajeRepository implements MensajeRepositoryPort {

    public final List<MensajeModel> mensajes = new ArrayList<>();
    public final Set<String> audiosExistentes = new LinkedHashSet<>();
    public final Map<java.util.UUID, OffsetDateTime> marcadosEliminados = new LinkedHashMap<>();
    /** (mensaje, usuario) ocultos para ese usuario: los demás siguen viéndolos. */
    public final Set<String> ocultosPorUsuario = new LinkedHashSet<>();
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
    public List<MensajeModel> findVisiblesByConversacionId(UUID conversacionId) {
        return findByConversacionId(conversacionId).stream()
                .filter(m -> !marcadosEliminados.containsKey(m.getId()))
                .toList();
    }

    @Override
    public List<MensajeModel> findVisiblesParaUsuario(UUID conversacionId, UUID usuarioId) {
        return findVisiblesByConversacionId(conversacionId).stream()
                .filter(m -> !ocultosPorUsuario.contains(claveOculto(m.getId(), usuarioId)))
                .toList();
    }

    private static String claveOculto(UUID mensajeId, UUID usuarioId) {
        return mensajeId + "|" + usuarioId;
    }

    @Override
    public Optional<MensajeModel> findUltimoMensaje(UUID conversacionId, UUID usuarioId) {
        List<MensajeModel> visibles = findVisiblesParaUsuario(conversacionId, usuarioId);
        return visibles.isEmpty() ? Optional.empty() : Optional.of(visibles.get(visibles.size() - 1));
    }

    @Override
    public int contarMensajes(UUID conversacionId, UUID usuarioId) {
        return findVisiblesParaUsuario(conversacionId, usuarioId).size();
    }

    @Override
    public boolean existeAudioUrl(String audioUrl) {
        return audioUrl != null && audiosExistentes.contains(audioUrl);
    }

    @Override
    public void marcarEliminados(UUID conversacionId, OffsetDateTime momento) {
        llamadas.add("marcarEliminados");
        findByConversacionId(conversacionId).forEach(m -> marcadosEliminados.put(m.getId(), momento));
    }

    @Override
    public void desmarcarEliminados(UUID conversacionId) {
        llamadas.add("desmarcarEliminados");
        findByConversacionId(conversacionId).forEach(m -> marcadosEliminados.remove(m.getId()));
    }

    @Override
    public Optional<MensajeModel> findById(UUID mensajeId) {
        return mensajes.stream().filter(m -> mensajeId.equals(m.getId())).findFirst();
    }

    @Override
    public void ocultarParaUsuario(UUID mensajeId, UUID usuarioId) {
        llamadas.add("ocultarParaUsuario");
        ocultosPorUsuario.add(claveOculto(mensajeId, usuarioId));
    }

    @Override
    public void marcarEliminado(UUID mensajeId, OffsetDateTime momento) {
        llamadas.add("marcarEliminado");
        marcadosEliminados.put(mensajeId, momento);
    }

    @Override
    public void eliminarFisicamente(UUID mensajeId) {
        llamadas.add("eliminarFisicamente");
        mensajes.removeIf(m -> mensajeId.equals(m.getId()));
    }
}
