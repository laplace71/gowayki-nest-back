package com.gowayki.nest.infrastructure.rest;

import com.gowayki.nest.application.dto.*;
import com.gowayki.nest.application.usecase.*;
import com.gowayki.nest.infrastructure.websocket.ChatWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final CrearConversacionUseCase crearConversacionUseCase;
    private final EnviarMensajeTextoUseCase enviarMensajeTextoUseCase;
    private final EnviarMensajeAudioUseCase enviarMensajeAudioUseCase;
    private final ObtenerMensajesUseCase obtenerMensajesUseCase;
    private final ListarConversacionesUsuarioUseCase listarConversacionesUsuarioUseCase;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatController(CrearConversacionUseCase crearConversacionUseCase,
                          EnviarMensajeTextoUseCase enviarMensajeTextoUseCase,
                          EnviarMensajeAudioUseCase enviarMensajeAudioUseCase,
                          ObtenerMensajesUseCase obtenerMensajesUseCase,
                          ListarConversacionesUsuarioUseCase listarConversacionesUsuarioUseCase,
                          ChatWebSocketHandler chatWebSocketHandler) {
        this.crearConversacionUseCase = crearConversacionUseCase;
        this.enviarMensajeTextoUseCase = enviarMensajeTextoUseCase;
        this.enviarMensajeAudioUseCase = enviarMensajeAudioUseCase;
        this.obtenerMensajesUseCase = obtenerMensajesUseCase;
        this.listarConversacionesUsuarioUseCase = listarConversacionesUsuarioUseCase;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @PostMapping("/conversaciones")
    public ResponseEntity<?> crearConversacion(@RequestBody CrearConversacionRequestDto request) {
        try {
            ConversacionResponseDto response = crearConversacionUseCase.ejecutar(request);
            chatWebSocketHandler.emitirMensaje(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error al crear conversación: " + e.getMessage()));
        }
    }

    @GetMapping("/usuarios/{usuarioId}/conversaciones")
    public ResponseEntity<List<ConversacionResponseDto>> listarConversaciones(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(listarConversacionesUsuarioUseCase.ejecutar(usuarioId));
    }

    @GetMapping("/conversaciones/{conversacionId}/mensajes")
    public ResponseEntity<?> obtenerMensajes(@PathVariable UUID conversacionId) {
        try {
            List<MensajeResponseDto> mensajes = obtenerMensajesUseCase.ejecutar(conversacionId);
            return ResponseEntity.ok(mensajes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        }
    }

    @PostMapping("/conversaciones/{conversacionId}/mensajes/texto")
    public ResponseEntity<?> enviarMensajeTexto(@PathVariable UUID conversacionId,
                                                @RequestBody EnviarMensajeTextoRequestDto request) {
        try {
            MensajeResponseDto response = enviarMensajeTextoUseCase.ejecutar(conversacionId, request);
            chatWebSocketHandler.emitirMensaje(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error al enviar mensaje: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/conversaciones/{conversacionId}/mensajes/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarMensajeAudio(@PathVariable UUID conversacionId,
                                                @RequestParam("audio") MultipartFile file,
                                                @RequestParam("emisorId") UUID emisorId,
                                                @RequestParam(value = "duracionSegundos", required = false) Integer duracionSegundos) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", "El archivo de audio es requerido."));
            }

            String nombreOriginal = file.getOriginalFilename();
            String extension = ".m4a";
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            }

            byte[] bytes = file.getBytes();
            MensajeResponseDto response = enviarMensajeAudioUseCase.ejecutar(
                    conversacionId, emisorId, bytes, extension, duracionSegundos
            );
            chatWebSocketHandler.emitirMensaje(response);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("exito", false, "mensaje", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error de lectura de archivo: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("exito", false, "mensaje", "Error interno al enviar audio: " + e.getMessage()));
        }
    }
}

