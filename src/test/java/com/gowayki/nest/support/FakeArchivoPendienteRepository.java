package com.gowayki.nest.support;

import com.gowayki.nest.domain.model.ArchivoPendienteBorradoModel;
import com.gowayki.nest.domain.repository.ArchivoPendienteBorradoPort;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Cola de archivos pendientes en memoria. */
public class FakeArchivoPendienteRepository implements ArchivoPendienteBorradoPort {

    public final Map<UUID, ArchivoPendienteBorradoModel> pendientes = new LinkedHashMap<>();
    public final List<String> encoladas = new ArrayList<>();

    public UUID agregar(String ruta) {
        UUID id = UUID.randomUUID();
        pendientes.put(id, ArchivoPendienteBorradoModel.builder()
                .id(id)
                .ruta(ruta)
                .creadoEn(OffsetDateTime.now())
                .intentos(0)
                .build());
        return id;
    }

    @Override
    public void encolar(String ruta, String motivo) {
        if (ruta == null || ruta.isBlank()) {
            return;
        }
        boolean yaEsta = pendientes.values().stream().anyMatch(p -> ruta.equals(p.getRuta()));
        if (yaEsta) {
            return;
        }
        encoladas.add(ruta);
        UUID id = UUID.randomUUID();
        pendientes.put(id, ArchivoPendienteBorradoModel.builder()
                .id(id)
                .ruta(ruta)
                .motivo(motivo)
                .creadoEn(OffsetDateTime.now())
                .intentos(0)
                .build());
    }

    @Override
    public List<ArchivoPendienteBorradoModel> pendientes(int limite) {
        List<ArchivoPendienteBorradoModel> lista = new ArrayList<>(pendientes.values());
        return lista.subList(0, Math.min(limite, lista.size()));
    }

    @Override
    public void quitar(UUID id) {
        pendientes.remove(id);
    }

    @Override
    public void registrarFallo(UUID id, String error) {
        ArchivoPendienteBorradoModel p = pendientes.get(id);
        if (p != null) {
            p.setIntentos(p.getIntentos() == null ? 1 : p.getIntentos() + 1);
            p.setUltimoError(error);
        }
    }
}
