package com.gowayki.nest.application.usecase;

import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.repository.ChatNotificacionPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Recorre las solicitudes vencidas y ejecuta cada una en su propia transacción,
 * para que un fallo aislado no arrastre a las demás.
 *
 * No depende de un temporizador en memoria: la fuente de verdad es la tabla
 * `conversacion_eliminacion`, así que un reinicio del backend no pierde nada.
 */
@Service
public class ProcesarEliminacionesGrupoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcesarEliminacionesGrupoUseCase.class);

    private final EliminacionGrupoPort eliminacionGrupoPort;
    private final EjecutarEliminacionGrupoUseCase ejecutarEliminacionGrupoUseCase;
    private final ChatNotificacionPort chatNotificacionPort;

    public ProcesarEliminacionesGrupoUseCase(EliminacionGrupoPort eliminacionGrupoPort,
                                             EjecutarEliminacionGrupoUseCase ejecutarEliminacionGrupoUseCase,
                                             ChatNotificacionPort chatNotificacionPort) {
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.ejecutarEliminacionGrupoUseCase = ejecutarEliminacionGrupoUseCase;
        this.chatNotificacionPort = chatNotificacionPort;
    }

    /** @return cuántas solicitudes se ejecutaron. */
    public int ejecutar() {
        List<EliminacionGrupoModel> vencidas = eliminacionGrupoPort.pendientesVencidas(OffsetDateTime.now());
        int ejecutadas = 0;

        for (EliminacionGrupoModel solicitud : vencidas) {
            try {
                var resultado = ejecutarEliminacionGrupoUseCase.ejecutar(solicitud.getId());
                if (resultado.isPresent()) {
                    notificar(resultado.get());
                    ejecutadas++;
                }
            } catch (Exception e) {
                log.error("No se pudo ejecutar la eliminación del grupo de la solicitud {}", solicitud.getId(), e);
            }
        }

        return ejecutadas;
    }

    private void notificar(EjecutarEliminacionGrupoUseCase.Resultado resultado) {
        UUID conversacionId = resultado.conversacionId();
        List<UUID> participantes = resultado.participantes();
        try {
            chatNotificacionPort.grupoEliminado(conversacionId, participantes);
        } catch (Exception e) {
            log.warn("El grupo {} se eliminó pero no se pudo avisar por WebSocket", conversacionId, e);
        }
    }
}
