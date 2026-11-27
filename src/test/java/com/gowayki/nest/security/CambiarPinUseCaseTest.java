package com.gowayki.nest.security;

import com.gowayki.nest.application.usecase.CambiarPinUseCase;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.support.FakeUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CambiarPinUseCaseTest {

    private FakeUsuarioRepository usuarios;
    private CambiarPinUseCase cambiarPin;
    private UUID choferId;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void preparar() {
        usuarios = new FakeUsuarioRepository();
        encoder = new BCryptPasswordEncoder();
        cambiarPin = new CambiarPinUseCase(usuarios, encoder);
        choferId = UUID.randomUUID();
        usuarios.save(UsuarioModel.builder()
                .id(choferId)
                .nombre("Chofer")
                .dni("71234567")
                .activo(true)
                .pinHash(encoder.encode("123456"))
                .build());
    }

    @Test
    void cambiaElPinCuandoElActualEsCorrecto() {
        cambiarPin.ejecutar(choferId, "123456", "654321");

        String hash = usuarios.findById(choferId).orElseThrow().getPinHash();
        assertTrue(encoder.matches("654321", hash));
    }

    @Test
    void rechazaElPinActualIncorrecto() {
        assertThrows(IllegalArgumentException.class,
                () -> cambiarPin.ejecutar(choferId, "000000", "654321"));
        assertTrue(encoder.matches("123456", usuarios.findById(choferId).orElseThrow().getPinHash()));
    }

    @Test
    void exigeSeisDigitos() {
        assertThrows(IllegalArgumentException.class,
                () -> cambiarPin.ejecutar(choferId, "123456", "1234"));
    }
}
