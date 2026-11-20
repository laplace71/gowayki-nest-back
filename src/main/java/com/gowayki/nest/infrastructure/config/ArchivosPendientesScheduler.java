package com.gowayki.nest.infrastructure.config;

import com.gowayki.nest.application.usecase.LimpiarArchivosPendientesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reintenta el borrado físico de los archivos que quedaron en la cola.
 *
 * Como la cola vive en la base, un reinicio del backend no pierde pendientes.
 */
@Component
public class ArchivosPendientesScheduler {

    private static final Logger log = LoggerFactory.getLogger(ArchivosPendientesScheduler.class);

    private final LimpiarArchivosPendientesUseCase limpiarArchivosPendientesUseCase;

    public ArchivosPendientesScheduler(LimpiarArchivosPendientesUseCase limpiarArchivosPendientesUseCase) {
        this.limpiarArchivosPendientesUseCase = limpiarArchivosPendientesUseCase;
    }

    @Scheduled(fixedDelayString = "${app.archivos.intervalo-ms:300000}",
            initialDelayString = "${app.archivos.retraso-inicial-ms:90000}")
    public void limpiar() {
        try {
            int borrados = limpiarArchivosPendientesUseCase.ejecutar();
            if (borrados > 0) {
                log.info("Archivos pendientes: {} borrado(s).", borrados);
            }
        } catch (Exception e) {
            log.error("Falló la limpieza de archivos pendientes", e);
        }
    }
}
