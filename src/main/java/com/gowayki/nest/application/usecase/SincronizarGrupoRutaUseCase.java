package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.domain.model.JefeSubrutaModel;
import com.gowayki.nest.domain.repository.AsignacionTurnoRepositoryPort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Sincroniza la pertenencia al grupo de ruta con la subruta REAL del turno.
 *
 * Antes esto dependía de que la app llamara a crearConversacion al entrar a la
 * subruta. Si esa llamada no llegaba, el conductor seguía participando en el
 * grupo de la subruta anterior. Ahora el backend lo corrige al asignar o cambiar
 * el turno.
 */
@Service
public class SincronizarGrupoRutaUseCase {

    private final CrearConversacionUseCase crearConversacionUseCase;
    private final AsignacionTurnoRepositoryPort turnoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final ConversacionRepositoryPort conversacionRepositoryPort;

    public SincronizarGrupoRutaUseCase(CrearConversacionUseCase crearConversacionUseCase,
                                       AsignacionTurnoRepositoryPort turnoRepositoryPort,
                                       UsuarioRepositoryPort usuarioRepositoryPort,
                                       ConversacionRepositoryPort conversacionRepositoryPort) {
        this.crearConversacionUseCase = crearConversacionUseCase;
        this.turnoRepositoryPort = turnoRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.conversacionRepositoryPort = conversacionRepositoryPort;
    }

    /**
     * El jefe de ruta es ADMIN del GRUPO_RUTA de la subruta que tiene asignada
     * en el panel. Se llama periódicamente: es idempotente y corrige datos
     * heredados donde el jefe había quedado como MIEMBRO.
     */
    public void sincronizarJefes() {
        for (JefeSubrutaModel jefe : usuarioRepositoryPort.findJefesConSubruta()) {
            String clave = jefe.getSubrutaCodigo();
            if (!esClaveDeSubrutaValida(clave)) {
                continue;
            }
            String nombre = jefe.getSubrutaNombre();
            String titulo = "GRUPO · " + (nombre != null && !nombre.isBlank() ? nombre.trim() : clave.trim());
            ConversacionResponseDto grupo = crearConversacionUseCase.asegurarGrupoRuta(
                    jefe.getChoferId(), clave.trim(), titulo);
            // Un jefe puede no ser el creador del grupo (ej. lo creó un chofer
            // antes de que el panel asignara la subruta). Se asegura el rol.
            conversacionRepositoryPort.cambiarRol(grupo.getId(), jefe.getChoferId(), "ADMIN");
        }
    }

    /** Sincroniza usando la subruta que ya trae el turno recién guardado. */
    public void paraTurno(AsignacionTurnoModel turno) {
        if (turno == null || turno.getChoferId() == null) {
            return;
        }
        String clave = turno.getSubrutaCodigo();
        if (!esClaveDeSubrutaValida(clave)) {
            // Datos heredados pueden traer un código de LÍNEA (C2, "Línea C3")
            // o basura como "GRUPO_RUTA" en la subruta del turno. Eso nunca fue
            // un grupo de ruta válido: se ignora para no romper el alta/cambio
            // de turno. La app ya descarta esas claves por su lado.
            return;
        }
        String nombre = turno.getSubrutaNombre();
        String titulo = "GRUPO · " + (nombre != null && !nombre.isBlank() ? nombre.trim() : clave.trim());
        crearConversacionUseCase.asegurarGrupoRuta(turno.getChoferId(), clave.trim(), titulo);
    }

    /** Sincroniza buscando el turno activo del conductor (para otros flujos). */
    public void paraChofer(UUID choferId) {
        if (choferId == null) {
            return;
        }
        turnoRepositoryPort.findTurnoActivoPorChofer(choferId).ifPresent(this::paraTurno);
    }

    /** Misma regla que CrearConversacionUseCase: SB-… sí, códigos de línea no. */
    private static boolean esClaveDeSubrutaValida(String claveRuta) {
        if (claveRuta == null || claveRuta.isBlank()) return false;
        String clave = claveRuta.trim();
        if ("GRUPO_RUTA".equalsIgnoreCase(clave)) return false;
        if (clave.matches("(?i)(l[ií]nea\\s+|canal\\s+de\\s+(l[ií]nea\\s+)?)?c\\d+")) return false;
        if (clave.regionMatches(true, 0, "GRUPO ", 0, 6)) return false;
        return true;
    }
}
