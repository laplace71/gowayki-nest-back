package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.EliminacionMensajeModel;
import com.gowayki.nest.domain.repository.EliminacionMensajePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/** Recorre las purgas vencidas, cada una en su propia transacción. */
@Service
public class ProcesarEliminacionesMensajeUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcesarEliminacionesMensajeUseCase.class);

    private final EliminacionMensajePort eliminacionMensajePort;
    private final EjecutarEliminacionMensajeUseCase ejecutarEliminacionMensajeUseCase;

    public ProcesarEliminacionesMensajeUseCase(EliminacionMensajePort eliminacionMensajePort,
                                               EjecutarEliminacionMensajeUseCase ejecutarEliminacionMensajeUseCase) {
        this.eliminacionMensajePort = eliminacionMensajePort;
        this.ejecutarEliminacionMensajeUseCase = ejecutarEliminacionMensajeUseCase;
    }

    /** @return cuántos mensajes se purgaron. */
    public int ejecutar() {
        List<EliminacionMensajeModel> vencidas = eliminacionMensajePort.pendientesVencidas(OffsetDateTime.now());
        int ejecutadas = 0;
        for (EliminacionMensajeModel solicitud : vencidas) {
            try {
                if (ejecutarEliminacionMensajeUseCase.ejecutar(solicitud.getId()).isPresent()) {
                    ejecutadas++;
                }
            } catch (Exception e) {
                log.error("No se pudo purgar el mensaje de la solicitud {}", solicitud.getId(), e);
            }
        }
        return ejecutadas;
    }
}
