package com.gowayki.nest.support;

import com.gowayki.nest.domain.repository.AudioStoragePort;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Disco simulado. `fallar` permite reproducir un borrado que no se pudo hacer.
 */
public class FakeAudioStorage implements AudioStoragePort {

    public final Set<String> archivos = new LinkedHashSet<>();
    public final List<String> borrados = new ArrayList<>();
    public boolean fallar = false;

    @Override
    public String guardarAudio(byte[] bytes, String extensionOriginal) {
        String url = "/uploads/audios/" + java.util.UUID.randomUUID() + extensionOriginal;
        archivos.add(url);
        return url;
    }

    @Override
    public byte[] obtenerAudio(String nombreArchivo) {
        return new byte[0];
    }

    @Override
    public boolean eliminarAudio(String archivoOUrl) {
        if (fallar) {
            return false;
        }
        archivos.remove(archivoOUrl);
        borrados.add(archivoOUrl);
        return true;
    }
}
