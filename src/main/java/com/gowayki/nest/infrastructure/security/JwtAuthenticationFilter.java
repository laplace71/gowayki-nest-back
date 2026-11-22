package com.gowayki.nest.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Toma el `Authorization: Bearer <jwt>` y publica la identidad en el
 * SecurityContext. Si el token falta o es inválido no autentica: las rutas
 * protegidas responden 401 y las públicas siguen funcionando.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepositoryPort usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepositoryPort usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            jwtService.validar(cabecera.substring(PREFIJO.length()).trim())
                    .ifPresent(identidad -> {
                        // Un token emitido antes de eliminar la cuenta queda
                        // revocado por tokenVersion y por el estado activo.
                        UsuarioModel usuario = usuarioRepository.findById(identidad.usuarioId()).orElse(null);
                        int versionActual = usuario == null || usuario.getTokenVersion() == null
                                ? 0 : usuario.getTokenVersion();
                        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())
                                || versionActual != identidad.tokenVersion()) {
                            return;
                        }
                        String rol = identidad.rol() == null ? "CHOFER" : identidad.rol();
                        UsernamePasswordAuthenticationToken autenticacion =
                                UsernamePasswordAuthenticationToken.authenticated(
                                        identidad.usuarioId(),
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                                );
                        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                        contexto.setAuthentication(autenticacion);
                        SecurityContextHolder.setContext(contexto);
                    });
        }
        filterChain.doFilter(request, response);
    }
}
