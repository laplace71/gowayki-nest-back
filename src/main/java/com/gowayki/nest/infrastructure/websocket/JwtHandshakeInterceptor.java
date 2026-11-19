package com.gowayki.nest.infrastructure.websocket;

import com.gowayki.nest.infrastructure.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Solo acepta el handshake del WebSocket si trae un JWT válido.
 *
 * Los eventos personales (ocultar/mostrar una conversación, avisos de
 * eliminación) se envían a las sesiones del usuario, así que la identidad no
 * puede venir de un `?usuarioId=` que cualquiera podría escribir.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATRIBUTO_USUARIO = "usuarioId";

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String cabecera = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            return false;
        }

        return jwtService.validar(cabecera.substring(PREFIJO.length()).trim())
                .map(identidad -> {
                    attributes.put(ATRIBUTO_USUARIO, identidad.usuarioId());
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Nada que limpiar.
    }
}
