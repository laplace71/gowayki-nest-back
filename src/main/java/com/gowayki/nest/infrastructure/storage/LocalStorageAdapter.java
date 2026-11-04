package com.gowayki.nest.infrastructure.storage;

import com.gowayki.nest.domain.repository.AudioStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalStorageAdapter implements AudioStoragePort {

    private final Path storageDirectory;

    public LocalStorageAdapter(@Value("${app.storage.upload-dir:uploads/audios}") String uploadDir) {
        this.storageDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar el directorio de almacenamiento", e);
        }
    }

    @Override
    public String guardarAudio(byte[] bytes, String extensionOriginal) {
        String extension = (extensionOriginal != null && !extensionOriginal.isBlank())
                ? (extensionOriginal.startsWith(".") ? extensionOriginal : "." + extensionOriginal)
                : ".m4a";

        String nombreArchivo = UUID.randomUUID() + extension;
        Path destino = this.storageDirectory.resolve(nombreArchivo);

        try {
            Files.write(destino, bytes);
            return "/uploads/audios/" + nombreArchivo;
        } catch (IOException e) {
            throw new IllegalStateException("Error al guardar el archivo de audio en disco", e);
        }
    }

    @Override
    public byte[] obtenerAudio(String nombreArchivo) {
        try {
            Path archivo = this.storageDirectory.resolve(nombreArchivo).normalize();
            if (!Files.exists(archivo)) {
                throw new IllegalArgumentException("El archivo de audio no existe.");
            }
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el archivo de audio", e);
        }
    }
}

