package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.*;
import com.gowayki.nest.application.usecase.*;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.domain.repository.ChatNotificacionPort;
import com.gowayki.nest.infrastructure.websocket.ChatWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Chat autenticado: el usuario siempre sale del JWT (`Authentication`), nunca
 * de un parámetro de la petición.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final CrearConversacionUseCase crearConversacionUseCase;
    private final EnviarMensajeTextoUseCase enviarMensajeTextoUseCase;
    private final EnviarMensajeAudioUseCase enviarMensajeAudioUseCase;
    private final ObtenerMensajesUseCase obtenerMensajesUseCase;
    private final ListarConversacionesUsuarioUseCase listarConversacionesUsuarioUseCase;
    private final OcultarConversacionUseCase ocultarConversacionUseCase;
    private final MostrarConversacionUseCase mostrarConversacionUseCase;
    private final SalirConversacionUseCase salirConversacionUseCase;
    private final AsignarRolParticipanteUseCase asignarRolParticipanteUseCase;
    private final AgregarParticipantesUseCase agregarParticipantesUseCase;
    private final ListarParticipantesConversacionUseCase listarParticipantesConversacionUseCase;
    private final SolicitarEliminacionGrupoUseCase solicitarEliminacionGrupoUseCase;
    private final CancelarEliminacionGrupoUseCase cancelarEliminacionGrupoUseCase;
    private final LimpiarConversacionUseCase limpiarConversacionUseCase;
    private final OcultarMensajeUseCase ocultarMensajeUseCase;
    private final EliminarMensajeParaTodosUseCase eliminarMensajeParaTodosUseCase;
    private final ChatNotificacionPort chatNotificacionPort;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatController(CrearConversacionUseCase crearConversacionUseCase,
                          EnviarMensajeTextoUseCase enviarMensajeTextoUseCase,
                          EnviarMensajeAudioUseCase enviarMensajeAudioUseCase,
                          ObtenerMensajesUseCase obtenerMensajesUseCase,
                          ListarConversacionesUsuarioUseCase listarConversacionesUsuarioUseCase,
                          OcultarConversacionUseCase ocultarConversacionUseCase,
                          MostrarConversacionUseCase mostrarConversacionUseCase,
                          SalirConversacionUseCase salirConversacionUseCase,
                          AsignarRolParticipanteUseCase asignarRolParticipanteUseCase,
                          AgregarParticipantesUseCase agregarParticipantesUseCase,
                          ListarParticipantesConversacionUseCase listarParticipantesConversacionUseCase,
                          SolicitarEliminacionGrupoUseCase solicitarEliminacionGrupoUseCase,
                          CancelarEliminacionGrupoUseCase cancelarEliminacionGrupoUseCase,
                          LimpiarConversacionUseCase limpiarConversacionUseCase,
                          OcultarMensajeUseCase ocultarMensajeUseCase,
                          EliminarMensajeParaTodosUseCase eliminarMensajeParaTodosUseCase,
                          ChatNotificacionPort chatNotificacionPort,
                          ChatWebSocketHandler chatWebSocketHandler) {
        this.crearConversacionUseCase = crearConversacionUseCase;
        this.enviarMensajeTextoUseCase = enviarMensajeTextoUseCase;
        this.enviarMensajeAudioUseCase = enviarMensajeAudioUseCase;
        this.obtenerMensajesUseCase = obtenerMensajesUseCase;
        this.listarConversacionesUsuarioUseCase = listarConversacionesUsuarioUseCase;
        this.ocultarConversacionUseCase = ocultarConversacionUseCase;
        this.mostrarConversacionUseCase = mostrarConversacionUseCase;
        this.salirConversacionUseCase = salirConversacionUseCase;
        this.asignarRolParticipanteUseCase = asignarRolParticipanteUseCase;
        this.agregarParticipantesUseCase = agregarParticipantesUseCase;
        this.listarParticipantesConversacionUseCase = listarParticipantesConversacionUseCase;
        this.solicitarEliminacionGrupoUseCase = solicitarEliminacionGrupoUseCase;
        this.cancelarEliminacionGrupoUseCase = cancelarEliminacionGrupoUseCase;
        this.limpiarConversacionUseCase = limpiarConversacionUseCase;
        this.ocultarMensajeUseCase = ocultarMensajeUseCase;
        this.eliminarMensajeParaTodosUseCase = eliminarMensajeParaTodosUseCase;
        this.chatNotificacionPort = chatNotificacionPort;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @PostMapping("/conversaciones")
    public ResponseEntity<ConversacionResponseDto> crearConversacion(@RequestBody CrearConversacionRequestDto request,
                                                                     Authentication autenticacion) {
        // El creador es siempre quien llama: el campo del cliente se ignora.
        request.setCreatedBy(usuario(autenticacion));
        ConversacionResponseDto response = crearConversacionUseCase.ejecutar(request);
        chatWebSocketHandler.emitirMensaje(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversaciones")
    public ResponseEntity<List<ConversacionResponseDto>> listarConversaciones(Authentication autenticacion) {
        return ResponseEntity.ok(listarConversacionesUsuarioUseCase.ejecutar(usuario(autenticacion)));
    }

    @GetMapping("/conversaciones/{conversacionId}/participantes")
    public ResponseEntity<List<ParticipanteResponseDto>> listarParticipantes(@PathVariable UUID conversacionId,
                                                                            Authentication autenticacion) {
        return ResponseEntity.ok(
                listarParticipantesConversacionUseCase.ejecutar(conversacionId, usuario(autenticacion)));
    }

    @GetMapping("/conversaciones/{conversacionId}/mensajes")
    public ResponseEntity<List<MensajeResponseDto>> obtenerMensajes(@PathVariable UUID conversacionId,
                                                                   Authentication autenticacion) {
        return ResponseEntity.ok(obtenerMensajesUseCase.ejecutar(conversacionId, usuario(autenticacion)));
    }

    /**
     * "Eliminar para mí": el mensaje desaparece solo para quien lo pide. Se
     * guarda en BD, sobrevive a reinstalar y los demás participantes no notan
     * nada. 204 sin cuerpo: no hay plazos ni detalles que mostrar al usuario.
     */
    @DeleteMapping("/conversaciones/{conversacionId}/mensajes/{mensajeId}")
    public ResponseEntity<Void> ocultarMensaje(@PathVariable UUID conversacionId,
                                               @PathVariable UUID mensajeId,
                                               Authentication autenticacion) {
        UUID usuarioId = usuario(autenticacion);
        ocultarMensajeUseCase.ejecutar(conversacionId, mensajeId, usuarioId);
        chatNotificacionPort.mensajeOculto(conversacionId, mensajeId, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * "Eliminar para todos": solo el emisor puede hacerlo. El mensaje
     * desaparece ya para todos; la purga física ocurre en silencio a las 72 h.
     * Tampoco aquí se devuelve plazo ni mensaje al usuario.
     */
    @DeleteMapping("/conversaciones/{conversacionId}/mensajes/{mensajeId}/todos")
    public ResponseEntity<Void> eliminarMensajeParaTodos(@PathVariable UUID conversacionId,
                                                        @PathVariable UUID mensajeId,
                                                        Authentication autenticacion) {
        UUID usuarioId = usuario(autenticacion);
        List<UUID> participantes = eliminarMensajeParaTodosUseCase.ejecutar(conversacionId, mensajeId, usuarioId);
        chatNotificacionPort.mensajeEliminado(conversacionId, mensajeId, participantes);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversaciones/{conversacionId}/mensajes/texto")
    public ResponseEntity<MensajeResponseDto> enviarMensajeTexto(@PathVariable UUID conversacionId,
                                                                @RequestBody EnviarMensajeTextoRequestDto request,
                                                                Authentication autenticacion) {
        // El emisor es siempre quien llama: el `emisorId` del cuerpo se ignora.
        request.setEmisorId(usuario(autenticacion));
        MensajeResponseDto response = enviarMensajeTextoUseCase.ejecutar(conversacionId, request);
        chatWebSocketHandler.emitirMensaje(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/conversaciones/{conversacionId}/mensajes/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarMensajeAudio(@PathVariable UUID conversacionId,
                                                @RequestParam("audio") MultipartFile file,
                                                @RequestParam(value = "duracionSegundos", required = false)
                                                Integer duracionSegundos,
                                                Authentication autenticacion) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("exito", false, "mensaje", "El archivo de audio es requerido."));
        }

        String nombreOriginal = file.getOriginalFilename();
        String extension = ".m4a";
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error de lectura de archivo: " + e.getMessage()));
        }

        MensajeResponseDto response = enviarMensajeAudioUseCase.ejecutar(
                conversacionId, usuario(autenticacion), bytes, extension, duracionSegundos
        );
        chatWebSocketHandler.emitirMensaje(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Saca al usuario de la conversación. No la borra para los demás. */
    @PostMapping("/conversaciones/{conversacionId}/salir")
    public ResponseEntity<Void> salirConversacion(@PathVariable UUID conversacionId,
                                                  Authentication autenticacion) {
        boolean eliminada = salirConversacionUseCase.ejecutar(conversacionId, usuario(autenticacion));
        // Solo si el grupo quedó vacío y se eliminó entero tiene sentido avisar
        // a todos. Si no, el grupo sigue existiendo para los demás miembros y
        // quien salió ya lo quita de su lista con la respuesta de esta llamada.
        if (eliminada) {
            chatWebSocketHandler.emitirEvento(Map.of(
                    "tipo", "CONVERSACION_ELIMINADA",
                    "conversacionId", conversacionId
            ));
        }
        return ResponseEntity.noContent().build();
    }

    /** Cambia el rol de un participante (ADMIN o MIEMBRO). Solo un admin/creador puede hacerlo. */
    @PutMapping("/conversaciones/{conversacionId}/participantes/{miembroId}/rol")
    public ResponseEntity<Void> asignarRol(@PathVariable UUID conversacionId,
                                           @PathVariable UUID miembroId,
                                           @RequestParam String rol,
                                           Authentication autenticacion) {
        asignarRolParticipanteUseCase.ejecutar(conversacionId, usuario(autenticacion), miembroId, rol);
        return ResponseEntity.noContent().build();
    }

    /**
     * Invita conductores a un grupo ya existente (por id, sin depender de que
     * el backend reutilice el grupo por título o clave de ruta).
     */
    @PostMapping("/conversaciones/{conversacionId}/participantes")
    public ResponseEntity<Void> agregarParticipantes(@PathVariable UUID conversacionId,
                                                     @RequestBody AgregarParticipantesRequestDto request,
                                                     Authentication autenticacion) {
        agregarParticipantesUseCase.ejecutar(
                conversacionId, usuario(autenticacion), request == null ? null : request.getUsuarioIds());
        return ResponseEntity.noContent().build();
    }

    /**
     * "Eliminar chat" o "eliminar grupo" tal como lo ve el usuario: oculta la
     * conversación SOLO para él. No borra mensajes, audios, membresía ni la
     * conversación. Los demás participantes no notan nada.
     */
    @DeleteMapping("/conversaciones/{conversacionId}")
    public ResponseEntity<Void> ocultarConversacion(@PathVariable UUID conversacionId,
                                                    Authentication autenticacion) {
        UUID usuarioId = usuario(autenticacion);
        ocultarConversacionUseCase.ejecutar(conversacionId, usuarioId);

        // Evento personal: solo sus dispositivos quitan la conversación de la lista.
        chatWebSocketHandler.emitirEventoAUsuario(usuarioId, Map.of(
                "tipo", "CONVERSACION_OCULTA",
                "conversacionId", conversacionId
        ));
        return ResponseEntity.noContent().build();
    }

    /** Deshace la ocultación: la conversación vuelve a la lista del usuario. */
    @PostMapping("/conversaciones/{conversacionId}/mostrar")
    public ResponseEntity<Void> mostrarConversacion(@PathVariable UUID conversacionId,
                                                    Authentication autenticacion) {
        UUID usuarioId = usuario(autenticacion);
        mostrarConversacionUseCase.ejecutar(conversacionId, usuarioId);

        chatWebSocketHandler.emitirEventoAUsuario(usuarioId, Map.of(
                "tipo", "CONVERSACION_MOSTRADA",
                "conversacionId", conversacionId
        ));
        return ResponseEntity.noContent().build();
    }

    /**
     * Limpia un chat SOLO para este usuario. Guarda la marca en BD (sobrevive a
     * reinstalar la app) y avisa a sus otros dispositivos. No borra mensajes y
     * los demás participantes no notan nada.
     */
    @PostMapping("/conversaciones/{conversacionId}/limpiar")
    public ResponseEntity<Void> limpiarConversacion(@PathVariable UUID conversacionId,
                                                    Authentication autenticacion) {
        UUID usuarioId = usuario(autenticacion);
        limpiarConversacionUseCase.ejecutar(conversacionId, usuarioId);

        chatNotificacionPort.conversacionLimpiada(
                conversacionId, usuarioId, java.time.OffsetDateTime.now());
        return ResponseEntity.noContent().build();
    }

    /**
     * Eliminación DEFINITIVA de un grupo: desaparece YA de todas las apps y
     * queda marcado (soft delete) en la BD. A las 72 horas un job lo purga
     * físicamente junto con sus mensajes y archivos. Solo creador o admin.
     */
    @PostMapping("/conversaciones/{conversacionId}/eliminacion")
    public ResponseEntity<EliminacionGrupoResponseDto> solicitarEliminacion(
            @PathVariable UUID conversacionId,
            Authentication autenticacion) {

        UUID usuarioId = usuario(autenticacion);
        EliminacionGrupoModel solicitud = solicitarEliminacionGrupoUseCase.ejecutar(conversacionId, usuarioId);

        List<UUID> participantes = listarParticipantesConversacionUseCase
                .ejecutar(conversacionId, usuarioId).stream()
                .map(ParticipanteResponseDto::getUserId)
                .filter(Objects::nonNull)
                .toList();
        // El grupo desaparece YA de todas las interfaces (soft delete en BD).
        // El aviso de "pendiente" ya no se envía: para la app el grupo no existe.
        chatNotificacionPort.grupoEliminado(conversacionId, participantes);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(EliminacionGrupoResponseDto.builder()
                .conversacionId(conversacionId)
                .solicitudId(solicitud.getId())
                .estado(solicitud.getEstado())
                .solicitadoEn(solicitud.getSolicitadoEn())
                .seEliminaraEn(solicitud.getEjecutarDespues())
                .horasDeEspera(SolicitarEliminacionGrupoUseCase.HORAS_DE_ESPERA)
                .build());
    }

    /** Cancela la eliminación definitiva antes de que se cumplan las 72 horas. */
    @DeleteMapping("/conversaciones/{conversacionId}/eliminacion")
    public ResponseEntity<Void> cancelarEliminacion(@PathVariable UUID conversacionId,
                                                    Authentication autenticacion) {
        UUID usuarioId = usuario(autenticacion);
        cancelarEliminacionGrupoUseCase.ejecutar(conversacionId, usuarioId);

        List<UUID> participantes = listarParticipantesConversacionUseCase
                .ejecutar(conversacionId, usuarioId).stream()
                .map(ParticipanteResponseDto::getUserId)
                .filter(Objects::nonNull)
                .toList();
        chatNotificacionPort.eliminacionCancelada(conversacionId, participantes);

        return ResponseEntity.noContent().build();
    }

    /** Identidad autenticada. Si no hay token válido, Spring Security ya respondió 401. */
    private static UUID usuario(Authentication autenticacion) {
        if (autenticacion == null || autenticacion.getPrincipal() == null) {
            throw new NoAutenticadoException("Se requiere un usuario autenticado.");
        }
        return UUID.fromString(autenticacion.getPrincipal().toString());
    }
}
