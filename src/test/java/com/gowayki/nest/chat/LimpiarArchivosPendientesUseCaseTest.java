package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.LimpiarArchivosPendientesUseCase;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeAudioStorage;
import com.gowayki.nest.support.FakeMensajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El borrado físico de audios es diferido, idempotente y no toca archivos que
 * otro mensaje sigue usando.
 */
class LimpiarArchivosPendientesUseCaseTest {

    private FakeArchivoPendienteRepository cola;
    private FakeAudioStorage disco;
    private FakeMensajeRepository mensajes;
    private LimpiarArchivosPendientesUseCase limpiar;

    @BeforeEach
    void preparar() {
        cola = new FakeArchivoPendienteRepository();
        disco = new FakeAudioStorage();
        mensajes = new FakeMensajeRepository();
        limpiar = new LimpiarArchivosPendientesUseCase(cola, disco, mensajes, 100);
    }

    @Test
    void borraElArchivoYVaciaLaFila() {
        UUID id = cola.agregar("/uploads/audios/uno.m4a");

        assertEquals(1, limpiar.ejecutar());
        assertTrue(cola.pendientes.isEmpty());
        assertTrue(disco.borrados.contains("/uploads/audios/uno.m4a"));
        assertTrue(id != null);
    }

    @Test
    void noBorraUnArchivoQueOtroMensajeTodaviaUsa() {
        mensajes.agregarAudio(UUID.randomUUID(), UUID.randomUUID(), "/uploads/audios/compartido.m4a");
        cola.agregar("/uploads/audios/compartido.m4a");

        assertEquals(0, limpiar.ejecutar());
        assertTrue(disco.borrados.isEmpty());
        assertTrue(cola.pendientes.isEmpty(), "La fila se descarta para no repetir la comprobación");
    }

    @Test
    void siElDiscoFallaLaFilaQuedaConElIntentoContado() {
        disco.fallar = true;
        UUID id = cola.agregar("/uploads/audios/uno.m4a");

        assertEquals(0, limpiar.ejecutar());
        assertTrue(cola.pendientes.containsKey(id));
        assertEquals(1, cola.pendientes.get(id).getIntentos());
    }

    @Test
    void unArchivoQueYaNoExisteCuentaComoBorrado() {
        // `eliminarAudio` devuelve true también cuando el archivo no existía.
        cola.agregar("/uploads/audios/ya-no-esta.m4a");

        assertEquals(1, limpiar.ejecutar());
        assertTrue(cola.pendientes.isEmpty());
    }
}
