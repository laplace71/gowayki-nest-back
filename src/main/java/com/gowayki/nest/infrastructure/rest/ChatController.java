package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.*;
import com.gowayki.nest.application.usecase.*;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
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
    private final EliminarConversacionUseCase eliminarConversacionUseCase;
    private final SalirConversacionUseCase salirConversacionUseCase;
    private final AsignarRolParticipanteUseCase asignarRolParticipanteUseCase;
    private final ListarParticipantesConversacionUseCase listarParticipantesConversacionUseCase;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatController(CrearConversacionUseCase crearConversacionUseCase,
                          EnviarMensajeTextoUseCase enviarMensajeTextoUseCase,
                          EnviarMensajeAudioUseCase enviarMensajeAudioUseCase,
                          ObtenerMensajesUseCase obtenerMensajesUseCase,
                          ListarConversacionesUsuarioUseCase listarConversacionesUsuarioUseCase,
                          EliminarConversacionUseCase eliminarConversacionUseCase,
                          SalirConversacionUseCase salirConversacionUseCase,
                          AsignarRolParticipanteUseCase asignarRolParticipanteUseCase,
                          ListarParticipantesConversacionUseCase listarParticipantesConversacionUseCase,
                          ChatWebSocketHandler chatWebSocketHandler) {
        this.crearConversacionUseCase = crearConversacionUseCase;
        this.enviarMensajeTextoUseCase = enviarMensajeTextoUseCase;
        this.enviarMensajeAudioUseCase = enviarMensajeAudioUseCase;
        this.obtenerMensajesUseCase = obtenerMensajesUseCase;
        this.listarConversacionesUsuarioUseCase = listarConversacionesUsuarioUseCase;
        this.eliminarConversacionUseCase = eliminarConversacionUseCase;
        this.salirConversacionUseCase = salirConversacionUseCase;
        this.asignarRolParticipanteUseCase = asignarRolParticipanteUseCase;
        this.listarParticipantesConversacionUseCase = listarParticipantesConversacionUseCase;
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

    @DeleteMapping("/conversaciones/{conversacionId}")
    public ResponseEntity<Void> eliminarConversacion(@PathVariable UUID conversacionId,
                                                     Authentication autenticacion) {
        eliminarConversacionUseCase.ejecutar(conversacionId, usuario(autenticacion));
        // Avisa a los demás dispositivos para que la quiten de su lista.
        chatWebSocketHandler.emitirEvento(Map.of(
                "tipo", "CONVERSACION_ELIMINADA",
                "conversacionId", conversacionId
        ));
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
