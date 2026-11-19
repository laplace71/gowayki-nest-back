package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.MostrarConversacionUseCase;
import com.gowayki.nest.application.usecase.OcultarConversacionUseCase;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.support.FakeConversacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "Eliminar chat" es personal: oculta, no borra. Estas pruebas fijan esa
 * garantía y los permisos.
 */
class OcultarConversacionUseCaseTest {

    private FakeConversacionRepository conversaciones;
    private OcultarConversacionUseCase ocultar;
    private MostrarConversacionUseCase mostrar;

    private UUID conversacionId;
    private UUID ana;
    private UUID beto;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        ocultar = new OcultarConversacionUseCase(conversaciones);
        mostrar = new MostrarConversacionUseCase(conversaciones);

        ana = UUID.randomUUID();
        beto = UUID.randomUUID();
        conversacionId = conversaciones.agregar(ConversacionModel.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .createdAt(OffsetDateTime.now())
                .build(), ana, beto);
    }

    @Test
    void sinIdentidadNoSePuedeOcultar() {
        assertThrows(NoAutenticadoException.class,
                () -> ocultar.ejecutar(conversacionId, null));
    }

    @Test
    void quienNoParticipaNoPuedeOcultar() {
        assertThrows(AccesoDenegadoException.class,
                () -> ocultar.ejecutar(conversacionId, UUID.randomUUID()));
    }

    @Test
    void ocultarSoloAfectaAQuienLoSolicitaYNoBorraNada() {
        ocultar.ejecutar(conversacionId, ana);

        assertTrue(conversaciones.ocultas.get(ana).contains(conversacionId));
        assertTrue(conversaciones.findByUsuarioId(ana).isEmpty(), "Ana ya no la ve");
        assertTrue(conversaciones.findByUsuarioId(beto).stream()
                        .anyMatch(c -> c.getId().equals(conversacionId)),
                "Beto sigue viendo el grupo");

        // Nada de borrado, ni de membresía, ni de roles.
        assertTrue(conversaciones.borradas.isEmpty());
        assertTrue(conversaciones.existeParticipante(conversacionId, ana));
        assertTrue(conversaciones.existeParticipante(conversacionId, beto));
        assertTrue(conversaciones.esAdministrador(conversacionId, ana));
    }

    @Test
    void mostrarDevuelveLaConversacionYEsIdempotente() {
        ocultar.ejecutar(conversacionId, ana);
        mostrar.ejecutar(conversacionId, ana);
        mostrar.ejecutar(conversacionId, ana);

        assertTrue(conversaciones.findByUsuarioId(ana).stream()
                .anyMatch(c -> c.getId().equals(conversacionId)));
        assertTrue(conversaciones.borradas.isEmpty());
    }

    @Test
    void mostrarExigeParticipar() {
        assertThrows(AccesoDenegadoException.class,
                () -> mostrar.ejecutar(conversacionId, UUID.randomUUID()));
    }
}
