package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.ArchivoPendienteBorradoModel;
import com.gowayki.nest.domain.repository.ArchivoPendienteBorradoPort;
import com.gowayki.nest.domain.repository.AudioStoragePort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Borra del disco los archivos que quedaron en la cola.
 *
 * Se ejecuta fuera de la transacción que borró los datos, así que un rollback
 * nunca deja archivos eliminados sin motivo. Es idempotente (un archivo que ya
 * no existe cuenta como borrado) y si el disco falla, la fila queda para el
 * siguiente intento.
 */
@Service
public class LimpiarArchivosPendientesUseCase {

    private static final Logger log = LoggerFactory.getLogger(LimpiarArchivosPendientesUseCase.class);

    private final ArchivoPendienteBorradoPort archivoPendienteBorradoPort;
    private final AudioStoragePort audioStoragePort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final int limite;

    public LimpiarArchivosPendientesUseCase(ArchivoPendienteBorradoPort archivoPendienteBorradoPort,
                                            AudioStoragePort audioStoragePort,
                                            MensajeRepositoryPort mensajeRepositoryPort,
                                            @Value("${app.archivos.limite-por-vuelta:200}") int limite) {
        this.archivoPendienteBorradoPort = archivoPendienteBorradoPort;
        this.audioStoragePort = audioStoragePort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.limite = limite;
    }

    /** @return cuántos archivos se borraron en esta vuelta. */
    public int ejecutar() {
        List<ArchivoPendienteBorradoModel> pendientes = archivoPendienteBorradoPort.pendientes(limite);
        int borrados = 0;

        for (ArchivoPendienteBorradoModel pendiente : pendientes) {
            String ruta = pendiente.getRuta();

            // Otro mensaje (u otra conversación) todavía apunta al mismo archivo:
            // no se borra. La fila se descarta para no repetir la comprobación.
            if (mensajeRepositoryPort.existeAudioUrl(ruta)) {
                archivoPendienteBorradoPort.quitar(pendiente.getId());
                continue;
            }

            try {
                if (audioStoragePort.eliminarAudio(ruta)) {
                    archivoPendienteBorradoPort.quitar(pendiente.getId());
                    borrados++;
                } else {
                    archivoPendienteBorradoPort.registrarFallo(pendiente.getId(), "El archivo no se pudo borrar");
                }
            } catch (Exception e) {
                log.warn("No se pudo borrar el archivo pendiente {}", ruta, e);
                archivoPendienteBorradoPort.registrarFallo(pendiente.getId(), e.getMessage());
            }
        }

        return borrados;
    }
}
