package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.LimpiarConversacionUseCase;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.support.FakeConversacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regla 1: limpiar es solo visual, personal y persistente (marca en BD). */
class LimpiarConversacionUseCaseTest {

    private FakeConversacionRepository conversaciones;
    private LimpiarConversacionUseCase limpiar;
    private UUID grupoId;
    private UUID chofer;
    private UUID otro;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        limpiar = new LimpiarConversacionUseCase(conversaciones);
        chofer = UUID.randomUUID();
        otro = UUID.randomUUID();
        grupoId = conversaciones.agregar(ConversacionModel.builder()
                .titulo("GRUPO · Prueba")
                .esGrupo(true)
                .createdBy(chofer)
                .createdAt(OffsetDateTime.now())
                .build(), chofer, otro);
    }

    @Test
    void guardaLaMarcaDelUsuarioSinBorrarLaConversacion() {
        limpiar.ejecutar(grupoId, chofer);

        assertTrue(conversaciones.limpiezasPorUsuario.containsKey(grupoId + "|" + chofer));
        assertTrue(conversaciones.borradas.isEmpty(), "No se borra la conversación");
        assertTrue(conversaciones.conversaciones.containsKey(grupoId));
    }

    @Test
    void laMarcaEsPorUsuarioYNoAfectaALosDemas() {
        limpiar.ejecutar(grupoId, chofer);

        assertTrue(conversaciones.limpiezasPorUsuario.containsKey(grupoId + "|" + chofer));
        assertTrue(!conversaciones.limpiezasPorUsuario.containsKey(grupoId + "|" + otro),
                "El otro participante no se ve afectado");
    }

    @Test
    void quienNoParticipaNoPuedeLimpiar() {
        assertThrows(AccesoDenegadoException.class,
                () -> limpiar.ejecutar(grupoId, UUID.randomUUID()));
    }
}
