package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.CancelarEliminacionGrupoUseCase;
import com.gowayki.nest.application.usecase.EjecutarEliminacionGrupoUseCase;
import com.gowayki.nest.application.usecase.ProcesarEliminacionesGrupoUseCase;
import com.gowayki.nest.application.usecase.SolicitarEliminacionGrupoUseCase;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.EliminacionGrupoModel;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeChatNotificacion;
import com.gowayki.nest.support.FakeConversacionRepository;
import com.gowayki.nest.support.FakeEliminacionGrupoRepository;
import com.gowayki.nest.support.FakeMensajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ciclo completo de la eliminación definitiva: pedir, cancelar y purgar a las
 * 72 horas, con revalidación en el momento de borrar.
 */
class EliminacionGrupoUseCaseTest {

    private FakeConversacionRepository conversaciones;
    private FakeEliminacionGrupoRepository eliminaciones;
    private FakeMensajeRepository mensajes;
    private FakeArchivoPendienteRepository archivos;
    private FakeChatNotificacion notificaciones;

    private SolicitarEliminacionGrupoUseCase solicitar;
    private CancelarEliminacionGrupoUseCase cancelar;
    private EjecutarEliminacionGrupoUseCase ejecutar;
    private ProcesarEliminacionesGrupoUseCase procesar;

    private UUID grupoId;
    private UUID creador;
    private UUID admin;
    private UUID miembro;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        eliminaciones = new FakeEliminacionGrupoRepository();
        mensajes = new FakeMensajeRepository();
        archivos = new FakeArchivoPendienteRepository();
        notificaciones = new FakeChatNotificacion();

        solicitar = new SolicitarEliminacionGrupoUseCase(conversaciones, eliminaciones, mensajes);
        cancelar = new CancelarEliminacionGrupoUseCase(conversaciones, eliminaciones, mensajes);
        ejecutar = new EjecutarEliminacionGrupoUseCase(conversaciones, mensajes, eliminaciones, archivos);
        procesar = new ProcesarEliminacionesGrupoUseCase(eliminaciones, ejecutar, notificaciones);

        creador = UUID.randomUUID();
        admin = UUID.randomUUID();
        miembro = UUID.randomUUID();

        grupoId = conversaciones.agregar(ConversacionModel.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .createdBy(creador)
                .createdAt(OffsetDateTime.now())
                .build(), creador, admin, miembro);

        // El segundo participante queda como ADMIN y el tercero como MIEMBRO.
        conversaciones.cambiarRol(grupoId, admin, "ADMIN");
        conversaciones.cambiarRol(grupoId, miembro, "MIEMBRO");
    }

    @Test
    void elCreadorPuedePedirLaEliminacionYSeProgramaA72Horas() {
        OffsetDateTime antes = OffsetDateTime.now();
        EliminacionGrupoModel solicitud = solicitar.ejecutar(grupoId, creador);

        assertEquals("PENDIENTE", solicitud.getEstado());
        assertEquals(grupoId, solicitud.getConversacionId());
        assertTrue(solicitud.getEjecutarDespues().isAfter(antes.plusHours(71)),
                "El plazo debe ser de 72 horas");
        assertFalse(conversaciones.borradas.contains(grupoId), "Pedir no borra nada todavía");
        assertTrue(conversaciones.marcadasEliminadas.containsKey(grupoId),
                "El grupo queda marcado (soft delete) y desaparece de las apps");
    }

    @Test
    void alPedirLaEliminacionLosMensajesQuedanMarcadosPeroSiguenEnLaBD() {
        mensajes.agregarAudio(grupoId, creador, "/uploads/audios/uno.m4a");

        solicitar.ejecutar(grupoId, creador);

        assertEquals(1, mensajes.marcadosEliminados.size());
        assertEquals(1, mensajes.findByConversacionId(grupoId).size(), "La purga todavía los ve");
        assertTrue(mensajes.findVisiblesByConversacionId(grupoId).isEmpty(), "La app ya no los ve");
    }

    @Test
    void pedirDosVecesNoDuplicaLaSolicitud() {
        EliminacionGrupoModel primera = solicitar.ejecutar(grupoId, creador);
        EliminacionGrupoModel segunda = solicitar.ejecutar(grupoId, admin);

        assertEquals(primera.getId(), segunda.getId());
        assertEquals(1, eliminaciones.solicitudes.size());
    }

    @Test
    void unMiembroNoPuedePedirLaEliminacion() {
        assertThrows(AccesoDenegadoException.class, () -> solicitar.ejecutar(grupoId, miembro));
    }

    @Test
    void sinIdentidadNoSePuedePedir() {
        assertThrows(NoAutenticadoException.class, () -> solicitar.ejecutar(grupoId, null));
    }

    @Test
    void unChatPrivadoNoSeEliminaDefinitivamente() {
        UUID privada = conversaciones.agregar(ConversacionModel.builder()
                .titulo("Privado · Marco")
                .esGrupo(false)
                .createdBy(creador)
                .createdAt(OffsetDateTime.now())
                .build(), creador, miembro);

        assertThrows(IllegalArgumentException.class, () -> solicitar.ejecutar(privada, creador));
    }

    @Test
    void elAdministradorPuedeCancelarAntesDelPlazo() {
        solicitar.ejecutar(grupoId, creador);
        cancelar.ejecutar(grupoId, admin);

        assertTrue(eliminaciones.solicitudes.values().stream()
                .anyMatch(s -> "CANCELADA".equals(s.getEstado())));
        assertTrue(conversaciones.borradas.isEmpty());
        assertFalse(conversaciones.marcadasEliminadas.containsKey(grupoId),
                "Al cancelar se deshace el soft delete y el grupo vuelve a verse");
    }

    @Test
    void unMiembroNoPuedeCancelar() {
        solicitar.ejecutar(grupoId, creador);
        assertThrows(AccesoDenegadoException.class, () -> cancelar.ejecutar(grupoId, miembro));
    }

    @Test
    void antesDeLas72HorasNoSeBorraNada() {
        UUID solicitudId = eliminaciones.agregarPendiente(grupoId, creador, OffsetDateTime.now().plusHours(1));

        assertTrue(ejecutar.ejecutar(solicitudId).isEmpty());
        assertTrue(conversaciones.borradas.isEmpty());
    }

    @Test
    void cumplidoElPlazoSeBorraYLosAudiosQuedanEnColaParaDespues() {
        mensajes.agregarAudio(grupoId, creador, "/uploads/audios/uno.m4a");
        mensajes.agregarAudio(grupoId, admin, "/uploads/audios/dos.m4a");
        UUID solicitudId = eliminaciones.agregarPendiente(grupoId, creador, OffsetDateTime.now().minusMinutes(1));

        var resultado = ejecutar.ejecutar(solicitudId);

        assertTrue(resultado.isPresent());
        assertEquals(List.of(creador, admin, miembro), resultado.get().participantes());
        assertTrue(conversaciones.borradas.contains(grupoId));
        assertEquals(2, archivos.encoladas.size(), "Los audios se encolan, no se borran en la transacción");
    }

    @Test
    void siElSolicitantePerdioElPermisoNoSeBorra() {
        // El creador sale del grupo y queda sin poder: la solicitud no debe borrar.
        conversaciones.quitarParticipante(grupoId, creador);
        conversaciones.cambiarRol(grupoId, admin, "MIEMBRO");

        UUID solicitudId = eliminaciones.agregarPendiente(grupoId, creador, OffsetDateTime.now().minusMinutes(1));
        assertTrue(ejecutar.ejecutar(solicitudId).isEmpty());

        assertTrue(conversaciones.borradas.isEmpty());
        assertTrue(eliminaciones.solicitudes.values().stream()
                .anyMatch(s -> "CANCELADA".equals(s.getEstado())));
    }

    @Test
    void siLaConversacionYaNoExisteLaSolicitudSeCierraSinError() {
        conversaciones.borradas.add(grupoId);
        conversaciones.conversaciones.remove(grupoId);
        UUID solicitudId = eliminaciones.agregarPendiente(grupoId, creador, OffsetDateTime.now().minusMinutes(1));

        assertTrue(ejecutar.ejecutar(solicitudId).isEmpty());
        assertTrue(eliminaciones.solicitudes.values().stream()
                .anyMatch(s -> "EJECUTADA".equals(s.getEstado())));
    }

    @Test
    void laPurgaEncuentraLasVencidasEnLaBaseYNoEnMemoria() {
        mensajes.agregarAudio(grupoId, creador, "/uploads/audios/uno.m4a");
        eliminaciones.agregarPendiente(grupoId, creador, OffsetDateTime.now().minusMinutes(1));
        eliminaciones.agregarPendiente(UUID.randomUUID(), creador, OffsetDateTime.now().plusHours(48));

        assertEquals(1, procesar.ejecutar(), "Solo la vencida se ejecuta");
        assertTrue(notificaciones.conversacionesEliminadas.contains(grupoId));
        assertEquals(List.of(creador, admin, miembro), notificaciones.destinatarios.get(0),
                "Solo se avisa a los participantes del grupo");
    }
}
