package com.gowayki.nest.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Emite y valida los JWT del chat (HS256).
 *
 * El token solo lleva el identificador del usuario y su rol; no se guarda nada
 * en el servidor, así que cualquier instancia del backend puede validarlo.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long duracionMillis;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-hours:168}") long horas) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 bytes para firmar con HS256.");
        }
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionMillis = TimeUnit.HOURS.toMillis(Math.max(1, horas));
    }

    public String generar(UUID usuarioId, String rol, Integer tokenVersion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("rol", rol)
                .claim("ver", tokenVersion == null ? 0 : tokenVersion)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(duracionMillis)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el usuario del token, o vacío si está vencido, alterado o mal formado. */
    public Optional<IdentidadToken> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number ver = claims.get("ver", Number.class);
            return Optional.of(new IdentidadToken(
                    UUID.fromString(claims.getSubject()),
                    claims.get("rol", String.class),
                    ver == null ? 0 : ver.intValue()
            ));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record IdentidadToken(UUID usuarioId, String rol, int tokenVersion) {
    }
}
