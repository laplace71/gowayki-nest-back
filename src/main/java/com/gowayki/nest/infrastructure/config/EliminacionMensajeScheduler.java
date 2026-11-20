package com.gowayki.nest.infrastructure.config;

import com.gowayki.nest.application.usecase.ProcesarEliminacionesMensajeUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Purga silenciosa de los mensajes borrados para todos hace más de 72 horas.
 * No avisa a nadie: para el usuario el mensaje ya desapareció al pedirlo.
 */
@Component
public class EliminacionMensajeScheduler {

    private static final Logger log = LoggerFactory.getLogger(EliminacionMensajeScheduler.class);

    private final ProcesarEliminacionesMensajeUseCase procesarEliminacionesMensajeUseCase;

    public EliminacionMensajeScheduler(ProcesarEliminacionesMensajeUseCase procesarEliminacionesMensajeUseCase) {
        this.procesarEliminacionesMensajeUseCase = procesarEliminacionesMensajeUseCase;
    }

    @Scheduled(fixedDelayString = "${app.eliminacion-mensajes.intervalo-ms:300000}",
            initialDelayString = "${app.eliminacion-mensajes.retraso-inicial-ms:60000}")
    public void purgar() {
        try {
            int ejecutadas = procesarEliminacionesMensajeUseCase.ejecutar();
            if (ejecutadas > 0) {
                log.info("Eliminación de mensajes: {} mensaje(s) purgado(s).", ejecutadas);
            }
        } catch (Exception e) {
            log.error("Falló la purga de mensajes pendientes", e);
        }
    }
}
