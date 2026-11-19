package com.gowayki.nest.domain.repository;

public interface AudioStoragePort {
    String guardarAudio(byte[] bytes, String extensionOriginal);
    byte[] obtenerAudio(String nombreArchivo);

    /**
     * Borra un audio del disco. Acepta tanto la URL guardada en la base
     * ("/uploads/audios/x.m4a") como el nombre del archivo ("x.m4a").
     *
     * @return true si el archivo ya no está (borrado o inexistente), false si
     *         no se pudo borrar y conviene reintentarlo.
     */
    boolean eliminarAudio(String archivoOUrl);
}
