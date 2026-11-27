package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.CrearConversacionUseCase;
import com.gowayki.nest.application.usecase.DepurarGruposObsoletosUseCase;
import com.gowayki.nest.application.usecase.MensajeSistemaService;
import com.gowayki.nest.application.usecase.SincronizarGrupoRutaUseCase;
import com.gowayki.nest.application.usecase.SolicitarEliminacionGrupoUseCase;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeAsignacionTurnoRepository;
import com.gowayki.nest.support.FakeChatNotificacion;
import com.gowayki.nest.support.FakeConversacionRepository;
import com.gowayki.nest.support.FakeEliminacionGrupoRepository;
import com.gowayki.nest.support.FakeMensajeRepository;
import com.gowayki.nest.support.FakeUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepurarGruposObsoletosUseCaseTest {

    private FakeConversacionRepository conversaciones;
    private FakeEliminacionGrupoRepository eliminaciones;
    private FakeChatNotificacion notificaciones;
    private FakeAsignacionTurnoRepository turnos;
    private DepurarGruposObsoletosUseCase depurar;

    private UUID admin;
    private UUID chofer;
    private UUID grupoSistema;
    private UUID grupoValido;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        eliminaciones = new FakeEliminacionGrupoRepository();
        notificaciones = new FakeChatNotificacion();
        turnos = new FakeAsignacionTurnoRepository();
        FakeMensajeRepository mensajes = new FakeMensajeRepository();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();

        admin = usuarios.agregar("Admin");
        chofer = usuarios.agregar("Conductor");

        CrearConversacionUseCase crear = new CrearConversacionUseCase(
                conversaciones, usuarios, mensajes,
                new FakeArchivoPendienteRepository(), eliminaciones,
                new MensajeSistemaService(mensajes, usuarios, conversaciones, notificaciones));
        SincronizarGrupoRutaUseCase sincronizar =
                new SincronizarGrupoRutaUseCase(crear, turnos, usuarios, conversaciones);
        SolicitarEliminacionGrupoUseCase solicitar = new SolicitarEliminacionGrupoUseCase(
                conversaciones, eliminaciones, mensajes);
        depurar = new DepurarGruposObsoletosUseCase(
                conversaciones, eliminaciones, solicitar, notificaciones, turnos, sincronizar);

        grupoSistema = conversaciones.agregar(ConversacionModel.builder()
                .titulo("system temp")
                .esGrupo(true)
                .tipoConversacion("GRUPO")
                .createdBy(admin)
                .createdAt(OffsetDateTime.now())
                .build(), admin);

        grupoValido = conversaciones.agregar(ConversacionModel.builder()
                .titulo("GRUPO · 4A")
                .esGrupo(true)
                .tipoConversacion("GRUPO_RUTA")
                .claveRuta("SB-4A")
                .createdBy(chofer)
                .createdAt(OffsetDateTime.now())
                .build(), chofer);

        turnos.agregar(chofer, "SB-4A", "4A");
    }

    @Test
    void marcaSoloLosGruposObsoletosYReconciliaLosTurnos() {
        DepurarGruposObsoletosUseCase.Resultado resultado = depurar.ejecutar(null, admin);

        assertTrue(resultado.getMarcados().contains(grupoSistema));
        assertFalse(resultado.getMarcados().contains(grupoValido));
        assertTrue(conversaciones.marcadasEliminadas.containsKey(grupoSistema));
        assertFalse(conversaciones.marcadasEliminadas.containsKey(grupoValido));
        assertTrue(eliminaciones.buscarPendiente(grupoSistema).isPresent());
        assertEquals(1, notificaciones.conversacionesEliminadas.size());
        assertEquals(1, resultado.getTurnosSincronizados());

        // El chofer sigue en su grupo de ruta correcto.
        assertTrue(conversaciones.existeParticipante(grupoValido, chofer));
    }
}
