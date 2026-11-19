package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.SalirConversacionUseCase;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeConversacionRepository;
import com.gowayki.nest.support.FakeMensajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Salir del grupo no borra el grupo para los demás y deja la administración cubierta. */
class SalirConversacionUseCaseTest {

    private FakeConversacionRepository conversaciones;
    private FakeMensajeRepository mensajes;
    private FakeArchivoPendienteRepository archivos;
    private SalirConversacionUseCase salir;

    private UUID grupoId;
    private UUID ana;
    private UUID beto;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        mensajes = new FakeMensajeRepository();
        archivos = new FakeArchivoPendienteRepository();
        salir = new SalirConversacionUseCase(conversaciones, mensajes, archivos);

        ana = UUID.randomUUID();
        beto = UUID.randomUUID();
        grupoId = conversaciones.agregar(ConversacionModel.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .createdBy(ana)
                .createdAt(OffsetDateTime.now())
                .build(), ana, beto);
    }

    @Test
    void salirNoBorraElGrupoMientrasQuedeAlguien() {
        assertFalse(salir.ejecutar(grupoId, ana));

        assertTrue(conversaciones.borradas.isEmpty());
        assertFalse(conversaciones.existeParticipante(grupoId, ana));
        assertTrue(conversaciones.existeParticipante(grupoId, beto));
    }

    @Test
    void siSeVaElUltimoAdministradorHeredaElMasAntiguo() {
        salir.ejecutar(grupoId, ana);

        assertTrue(conversaciones.esAdministrador(grupoId, beto),
                "El grupo no puede quedarse sin administrador");
        assertEquals(1, conversaciones.promociones());
    }

    @Test
    void elGrupoVacioSeEliminaYLosAudiosSeEncolan() {
        mensajes.agregarAudio(grupoId, ana, "/uploads/audios/uno.m4a");
        salir.ejecutar(grupoId, ana);
        assertTrue(salir.ejecutar(grupoId, beto));

        assertTrue(conversaciones.borradas.contains(grupoId));
        assertEquals(1, archivos.encoladas.size());
    }

    @Test
    void quienNoParticipaNoPuedeSalir() {
        assertThrows(AccesoDenegadoException.class,
                () -> salir.ejecutar(grupoId, UUID.randomUUID()));
    }

    @Test
    void salirBloqueaLaConversacionAntesDeTocarLaMembresia() {
        salir.ejecutar(grupoId, ana);
        assertTrue(conversaciones.llamadas.contains("bloquearParaActualizar"));
    }
}
