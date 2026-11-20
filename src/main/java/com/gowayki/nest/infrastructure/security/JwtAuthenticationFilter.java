package com.gowayki.nest.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            jwtService.validar(cabecera.substring(PREFIJO.length()).trim())
                    .ifPresent(identidad -> {
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
