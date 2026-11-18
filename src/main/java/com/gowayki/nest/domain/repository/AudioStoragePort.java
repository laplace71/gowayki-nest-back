package com.gowayki.nest.domain.repository;

public interface AudioStoragePort {
    String guardarAudio(byte[] bytes, String extensionOriginal);
    byte[] obtenerAudio(String nombreArchivo);

    /**
     * Borra un audio del disco. Acepta tanto la URL guardada en la base
     * ("/uploads/audios/x.m4a") como el nombre del archivo ("x.m4a").
     * Si el archivo ya no existe, no falla.
     */
    void eliminarAudio(String archivoOUrl);
}
