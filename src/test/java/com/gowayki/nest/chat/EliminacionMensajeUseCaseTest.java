package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.EjecutarEliminacionMensajeUseCase;
import com.gowayki.nest.application.usecase.EliminarMensajeParaTodosUseCase;
import com.gowayki.nest.application.usecase.OcultarMensajeUseCase;
import com.gowayki.nest.application.usecase.ProcesarEliminacionesMensajeUseCase;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeConversacionRepository;
import com.gowayki.nest.support.FakeEliminacionMensajeRepository;
import com.gowayki.nest.support.FakeMensajeRepository;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas de "eliminar para mí" y "eliminar para todos" a nivel dominio. */
class EliminacionMensajeUseCaseTest {

    private final UUID emisor = UUID.randomUUID();
    private final UUID otro = UUID.randomUUID();

    private FakeMensajeRepository mensajes;
    private FakeConversacionRepository conversaciones;
    private UUID conversacionId;

    private void setUp(String audioUrl) {
        mensajes = new FakeMensajeRepository();
        conversaciones = new FakeConversacionRepository();
        conversacionId = conversaciones.agregar(
                ConversacionModel.builder().titulo("Grupo").esGrupo(true).build(), emisor, otro);
        MensajeModel mensaje = MensajeModel.builder()
                .id(UUID.randomUUID())
                .conversacionId(conversacionId)
                .emisorId(emisor)
                .tipo(audioUrl == null ? "TEXTO" : "AUDIO")
                .contenidoTexto(audioUrl == null ? "Hola" : null)
                .audioUrl(audioUrl)
                .build();
        mensajes.save(mensaje);
    }

    private UUID mensajeId() {
        return mensajes.mensajes.get(0).getId();
    }

    @Test
    void ocultarParaMiNoAfectaALosDemas() {
        setUp(null);
        new OcultarMensajeUseCase(mensajes, conversaciones).ejecutar(conversacionId, mensajeId(), otro);

        assertFalse(mensajes.findVisiblesParaUsuario(conversacionId, otro).stream()
                .anyMatch(m -> m.getId().equals(mensajeId())));
        assertTrue(mensajes.findVisiblesParaUsuario(conversacionId, emisor).stream()
                .anyMatch(m -> m.getId().equals(mensajeId())));
        // La fila sigue existiendo: no se borró nada de la BD.
        assertEquals(1, mensajes.mensajes.size());
        assertFalse(mensajes.marcadosEliminados.containsKey(mensajeId()));
    }

    @Test
    void soloElEmisorPuedeEliminarParaTodos() {
        setUp(null);
        var useCase = new EliminarMensajeParaTodosUseCase(
                mensajes, conversaciones, new FakeEliminacionMensajeRepository());

        assertThrows(AccesoDenegadoException.class,
                () -> useCase.ejecutar(conversacionId, mensajeId(), otro));
    }

    @Test
    void eliminarParaTodosOcultaYaYProgramaLaPurga() {
        setUp(null);
        var eliminaciones = new FakeEliminacionMensajeRepository();
        List<UUID> avisados = new EliminarMensajeParaTodosUseCase(mensajes, conversaciones, eliminaciones)
                .ejecutar(conversacionId, mensajeId(), emisor);

        assertTrue(mensajes.marcadosEliminados.containsKey(mensajeId()));
        assertTrue(mensajes.findVisiblesByConversacionId(conversacionId).isEmpty());
        assertEquals(1, eliminaciones.solicitudes.size());
        var solicitud = eliminaciones.solicitudes.values().iterator().next();
        assertEquals(emisor, solicitud.getSolicitadoPor());
        assertEquals("PENDIENTE", solicitud.getEstado());
        assertTrue(solicitud.getEjecutarDespues().isAfter(OffsetDateTime.now().plusHours(71)));
        assertTrue(solicitud.getEjecutarDespues().isBefore(OffsetDateTime.now().plusHours(73)));
        assertEquals(2, avisados.size());
    }

    @Test
    void laPurgaEsSilenciosaYBorraElAudio() {
        setUp("/uploads/audios/nota.m4a");
        var eliminaciones = new FakeEliminacionMensajeRepository();
        var archivos = new FakeArchivoPendienteRepository();
        new EliminarMensajeParaTodosUseCase(mensajes, conversaciones, eliminaciones)
                .ejecutar(conversacionId, mensajeId(), emisor);

        // Antes del plazo no se purga nada.
        var procesar = new ProcesarEliminacionesMensajeUseCase(
                eliminaciones,
                new EjecutarEliminacionMensajeUseCase(eliminaciones, mensajes, archivos));
        assertEquals(0, procesar.ejecutar());
        assertEquals(1, mensajes.mensajes.size());

        // Vencidas las 72 h: se borra el mensaje y se encola el audio.
        OffsetDateTime futuro = OffsetDateTime.now().plusHours(73);
        procesar = new ProcesarEliminacionesMensajeUseCase(
                eliminaciones,
                new EjecutarEliminacionMensajeUseCase(eliminaciones, mensajes, archivos));
        // La consulta del fake ya devuelve la solicitud vencida; forzamos el reloj.
        eliminaciones.solicitudes.values().forEach(s -> s.setEjecutarDespues(OffsetDateTime.now().minusSeconds(1)));
        assertEquals(1, procesar.ejecutar());
        assertTrue(mensajes.mensajes.isEmpty());
        assertTrue(mensajes.llamadas.contains("eliminarFisicamente"));
        assertTrue(archivos.pendientes(10).stream()
                .anyMatch(p -> "/uploads/audios/nota.m4a".equals(p.getRuta())));
    }
}
