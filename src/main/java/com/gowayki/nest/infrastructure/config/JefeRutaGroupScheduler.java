package com.gowayki.nest.infrastructure.config;

import com.gowayki.nest.application.usecase.SincronizarGrupoRutaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Mantiene el GRUPO_RUTA de cada jefe alineado con la subruta que el panel
 * le asignó. Si el panel cambia la subruta, el jefe se mueve al grupo nuevo.
 */
@Component
public class JefeRutaGroupScheduler {

    private static final Logger log = LoggerFactory.getLogger(JefeRutaGroupScheduler.class);

    private final SincronizarGrupoRutaUseCase sincronizarGrupoRutaUseCase;

    public JefeRutaGroupScheduler(SincronizarGrupoRutaUseCase sincronizarGrupoRutaUseCase) {
        this.sincronizarGrupoRutaUseCase = sincronizarGrupoRutaUseCase;
    }

    @Scheduled(
        initialDelayString = "${app.jefes.intervalo-ms:10000}",
        fixedDelayString = "${app.jefes.intervalo-ms:60000}"
    )
    public void ejecutar() {
        try {
            sincronizarGrupoRutaUseCase.sincronizarJefes();
        } catch (Exception e) {
            log.warn("No se pudo sincronizar los grupos de los jefes de ruta: {}", e.getMessage());
        }
    }
}
