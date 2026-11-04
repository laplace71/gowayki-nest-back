package com.gowayki.nest.domain.repository;

public interface AudioStoragePort {
    String guardarAudio(byte[] bytes, String extensionOriginal);
    byte[] obtenerAudio(String nombreArchivo);
}
