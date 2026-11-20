package com.gowayki.nest.infrastructure.config;

import com.gowayki.nest.application.usecase.ProcesarEliminacionesGrupoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Purga periódica de los grupos cuya eliminación definitiva venció.
 *
 * La consulta sale de la base de datos, así que sobrevive a reinicios del
 * backend y del VPS: no hay estado en memoria que se pueda perder.
 */
@Component
public class EliminacionGrupoScheduler {

    private static final Logger log = LoggerFactory.getLogger(EliminacionGrupoScheduler.class);

    private final ProcesarEliminacionesGrupoUseCase procesarEliminacionesGrupoUseCase;

    public EliminacionGrupoScheduler(ProcesarEliminacionesGrupoUseCase procesarEliminacionesGrupoUseCase) {
        this.procesarEliminacionesGrupoUseCase = procesarEliminacionesGrupoUseCase;
    }

    @Scheduled(fixedDelayString = "${app.eliminacion.intervalo-ms:300000}",
            initialDelayString = "${app.eliminacion.retraso-inicial-ms:60000}")
    public void purgar() {
        try {
            int ejecutadas = procesarEliminacionesGrupoUseCase.ejecutar();
            if (ejecutadas > 0) {
                log.info("Eliminación definitiva: {} grupo(s) purgado(s).", ejecutadas);
            }
        } catch (Exception e) {
            log.error("Falló la purga de grupos pendientes", e);
        }
    }
}
