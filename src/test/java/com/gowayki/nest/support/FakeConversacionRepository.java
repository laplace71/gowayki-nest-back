package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.ParticipanteConversacion;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementación en memoria del puerto de conversaciones para los tests.
 *
 * No imita SQL: solo guarda el estado y apunta qué métodos se llamaron, que es
 * lo que hace falta para comprobar los permisos y los efectos de cada caso de
 * uso sin levantar una base de datos.
 */
public class FakeConversacionRepository implements ConversacionRepositoryPort {

    public final Map<UUID, ConversacionModel> conversaciones = new LinkedHashMap<>();
    public final Map<UUID, Map<UUID, String>> participantesPorConversacion = new HashMap<>();
    public final Map<UUID, Set<UUID>> ocultas = new HashMap<>();
    public final Map<UUID, OffsetDateTime> marcadasEliminadas = new LinkedHashMap<>();
    public final Map<String, OffsetDateTime> limpiezasPorUsuario = new LinkedHashMap<>();

    public final List<UUID> borradas = new ArrayList<>();
    public final List<String> llamadas = new ArrayList<>();

    private final Map<UUID, Integer> promociones = new HashMap<>();
    private boolean devolverPromocion = true;

    public UUID agregar(ConversacionModel conversacion, UUID... participantes) {
        UUID id = conversacion.getId() != null ? conversacion.getId() : UUID.randomUUID();
        conversacion.setId(id);
        conversaciones.put(id, conversacion);
        Map<UUID, String> miembros = new LinkedHashMap<>();
        for (int i = 0; i < participantes.length; i++) {
            miembros.put(participantes[i], i == 0 ? "ADMIN" : "MIEMBRO");
        }
        participantesPorConversacion.put(id, miembros);
        if (conversacion.getTipoConversacion() == null) {
            conversacion.setTipoConversacion(Boolean.TRUE.equals(conversacion.getEsGrupo()) ? "GRUPO" : "PRIVADA");
        }
        return id;
    }

    public void definirPromocion(boolean devolver) {
        this.devolverPromocion = devolver;
    }

    public int promociones() {
        return promociones.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public ConversacionModel save(ConversacionModel model, List<UUID> participanteIds) {
        llamadas.add("save");
        UUID id = agregar(model, participanteIds.toArray(new UUID[0]));
        return conversaciones.get(id);
    }

    @Override
    public Optional<ConversacionModel> findById(UUID id) {
        return Optional.ofNullable(conversaciones.get(id));
    }

    @Override
    public List<ConversacionModel> findByUsuarioId(UUID usuarioId) {
        return conversaciones.values().stream()
                .filter(c -> participantesPorConversacion.getOrDefault(c.getId(), Map.of()).containsKey(usuarioId))
                .filter(c -> !ocultas.getOrDefault(usuarioId, Set.of()).contains(c.getId()))
                .filter(c -> !marcadasEliminadas.containsKey(c.getId()))
                .toList();
    }

    @Override
    public List<ConversacionModel> findByUsuarioIdSinFiltroOcultas(UUID usuarioId) {
        return conversaciones.values().stream()
                .filter(c -> participantesPorConversacion.getOrDefault(c.getId(), Map.of()).containsKey(usuarioId))
                .filter(c -> !marcadasEliminadas.containsKey(c.getId()))
                .toList();
    }

    @Override
    public boolean existeParticipante(UUID conversacionId, UUID usuarioId) {
        return participantesPorConversacion.getOrDefault(conversacionId, Map.of()).containsKey(usuarioId);
    }

    @Override
    public Optional<ConversacionModel> findByTitulo(String titulo) {
        return conversaciones.values().stream()
                .filter(c -> titulo != null && titulo.equals(c.getTitulo()))
                .findFirst();
    }

    @Override
    public Optional<ConversacionModel> findByClaveRuta(String claveRuta) {
        return conversaciones.values().stream()
                .filter(c -> claveRuta != null && claveRuta.equals(c.getClaveRuta()))
                .findFirst();
    }

    @Override
    public void agregarParticipante(UUID conversacionId, UUID usuarioId) {
        llamadas.add("agregarParticipante:" + usuarioId);
        participantesPorConversacion
                .computeIfAbsent(conversacionId, k -> new LinkedHashMap<>())
                .putIfAbsent(usuarioId, "MIEMBRO");
    }

    @Override
    public int contarParticipantes(UUID conversacionId) {
        return participantesPorConversacion.getOrDefault(conversacionId, Map.of()).size();
    }

    @Override
    public boolean esAdministrador(UUID conversacionId, UUID usuarioId) {
        return "ADMIN".equals(obtenerRol(conversacionId, usuarioId));
    }

    @Override
    public String obtenerRol(UUID conversacionId, UUID usuarioId) {
        return participantesPorConversacion.getOrDefault(conversacionId, Map.of()).get(usuarioId);
    }

    @Override
    public void quitarParticipante(UUID conversacionId, UUID usuarioId) {
        llamadas.add("quitarParticipante:" + usuarioId);
        Map<UUID, String> miembros = participantesPorConversacion.get(conversacionId);
        if (miembros != null) {
            miembros.remove(usuarioId);
        }
    }

    @Override
    public void cambiarRol(UUID conversacionId, UUID usuarioId, String nuevoRol) {
        llamadas.add("cambiarRol:" + usuarioId + ":" + nuevoRol);
        participantesPorConversacion.getOrDefault(conversacionId, Map.of()).put(usuarioId, nuevoRol);
    }

    @Override
    public List<ParticipanteConversacion> listarParticipantes(UUID conversacionId) {
        return participantesPorConversacion.getOrDefault(conversacionId, Map.of()).entrySet().stream()
                .map(e -> ParticipanteConversacion.builder()
                        .userId(e.getKey())
                        .nombre("Usuario " + e.getKey())
                        .rol(e.getValue())
                        .build())
                .toList();
    }

    @Override
    public void eliminar(UUID conversacionId) {
        llamadas.add("eliminar");
        borradas.add(conversacionId);
        conversaciones.remove(conversacionId);
        participantesPorConversacion.remove(conversacionId);
    }

    @Override
    public void marcarEliminado(UUID conversacionId, OffsetDateTime momento) {
        llamadas.add("marcarEliminado");
        marcadasEliminadas.put(conversacionId, momento);
    }

    @Override
    public void desmarcarEliminado(UUID conversacionId) {
        llamadas.add("desmarcarEliminado");
        marcadasEliminadas.remove(conversacionId);
    }

    @Override
    public void ocultarParaUsuario(UUID conversacionId, UUID usuarioId) {
        llamadas.add("ocultarParaUsuario:" + usuarioId);
        ocultas.computeIfAbsent(usuarioId, k -> new LinkedHashSet<>()).add(conversacionId);
    }

    @Override
    public void mostrarParaUsuario(UUID conversacionId, UUID usuarioId) {
        llamadas.add("mostrarParaUsuario:" + usuarioId);
        Set<UUID> propias = ocultas.get(usuarioId);
        if (propias != null) {
            propias.remove(conversacionId);
        }
    }

    @Override
    public void limpiarParaUsuario(UUID conversacionId, UUID usuarioId, OffsetDateTime momento) {
        llamadas.add("limpiarParaUsuario:" + usuarioId);
        limpiezasPorUsuario.put(conversacionId + "|" + usuarioId, momento);
    }

    @Override
    public void limpiarEstadoUsuario(UUID conversacionId, UUID usuarioId) {
        llamadas.add("limpiarEstadoUsuario:" + usuarioId);
        Set<UUID> propias = ocultas.get(usuarioId);
        if (propias != null) {
            propias.remove(conversacionId);
        }
    }

    @Override
    public Optional<ConversacionModel> bloquearParaActualizar(UUID conversacionId) {
        llamadas.add("bloquearParaActualizar");
        return findById(conversacionId);
    }

    @Override
    public Optional<UUID> promoverAdministradorSiHaceFalta(UUID conversacionId) {
        llamadas.add("promoverAdministradorSiHaceFalta");
        if (!devolverPromocion) {
            return Optional.empty();
        }
        Map<UUID, String> miembros = participantesPorConversacion.getOrDefault(conversacionId, Map.of());
        if (miembros.isEmpty() || miembros.containsValue("ADMIN")) {
            return Optional.empty();
        }
        UUID elegido = miembros.keySet().iterator().next();
        miembros.put(elegido, "ADMIN");
        promociones.merge(conversacionId, 1, Integer::sum);
        return Optional.of(elegido);
    }
}
