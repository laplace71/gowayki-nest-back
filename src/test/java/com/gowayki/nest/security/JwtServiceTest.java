package com.gowayki.nest.security;

import com.gowayki.nest.infrastructure.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El token identifica al usuario. Si está alterado, vencido o firmado con otra
 * clave, no vale: es lo que separa "autenticado" de "cualquiera".
 */
class JwtServiceTest {

    private static final String SECRETO = "secreto-de-pruebas-con-mas-de-32-bytes-para-hs256";

    @Test
    void unTokenValidoDevuelveElUsuarioYSuRol() {
        JwtService servicio = new JwtService(SECRETO, 1);
        UUID usuarioId = UUID.randomUUID();

        String token = servicio.generar(usuarioId, "CHOFER", 0);
        Optional<JwtService.IdentidadToken> identidad = servicio.validar(token);

        assertTrue(identidad.isPresent());
        assertEquals(usuarioId, identidad.get().usuarioId());
        assertEquals("CHOFER", identidad.get().rol());
    }

    @Test
    void unTokenAlteradoNoVale() {
        JwtService servicio = new JwtService(SECRETO, 1);
        String token = servicio.generar(UUID.randomUUID(), "CHOFER", 0);
        String alterado = token.substring(0, token.length() - 2) + "aa";

        assertTrue(servicio.validar(alterado).isEmpty());
    }

    @Test
    void unTokenFirmadoConOtraClaveNoVale() {
        JwtService emisor = new JwtService(SECRETO, 1);
        JwtService otro = new JwtService("otro-secreto-distinto-con-mas-de-32-bytes!!", 1);

        String token = emisor.generar(UUID.randomUUID(), "ADMIN", 0);
        assertTrue(otro.validar(token).isEmpty());
    }

    @Test
    void unTokenVencidoNoVale() {
        JwtService servicio = new JwtService(SECRETO, 1);

        // Se firma a mano un token ya vencido con la misma clave.
        Instant ahora = Instant.now();
        String vencido = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("rol", "CHOFER")
                .issuedAt(Date.from(ahora.minusSeconds(7200)))
                .expiration(Date.from(ahora.minusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertTrue(servicio.validar(vencido).isEmpty());
    }

    @Test
    void unSecretoCortoSeRechazaAlArrancar() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corto", 1));
    }

    @Test
    void basuraNoLanzaExcepcionSoloNoVale() {
        JwtService servicio = new JwtService(SECRETO, 1);
        assertFalse(servicio.validar("esto-no-es-un-jwt").isPresent());
    }
}
