package com.gowayki.nest.chat;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.application.dto.EnviarMensajeTextoRequestDto;
import com.gowayki.nest.application.usecase.CambiarSubrutaTurnoUseCase;
import com.gowayki.nest.application.usecase.CrearConversacionUseCase;
import com.gowayki.nest.application.usecase.EnviarMensajeTextoUseCase;
import com.gowayki.nest.application.usecase.ListarConversacionesUsuarioUseCase;
import com.gowayki.nest.application.usecase.MensajeSistemaService;
import com.gowayki.nest.application.usecase.ObtenerMensajesUseCase;
import com.gowayki.nest.application.usecase.SincronizarGrupoRutaUseCase;
import com.gowayki.nest.application.usecase.ValidarSubrutaChoferUseCase;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeAsignacionTurnoRepository;
import com.gowayki.nest.support.FakeChatNotificacion;
import com.gowayki.nest.support.FakeConversacionRepository;
import com.gowayki.nest.support.FakeEliminacionGrupoRepository;
import com.gowayki.nest.support.FakeMensajeRepository;
import com.gowayki.nest.support.FakeUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Escenarios de separación por subruta:
 *  - dos conductores en la MISMA subruta comparten grupo y se ven los mensajes;
 *  - al cambiar uno de subruta, el grupo y los mensajes anteriores desaparecen
 *    para él, pero siguen intactos para el que se quedó;
 *  - nadie puede unirse al grupo de ruta de una subruta que no es la suya.
 */
class AislamientoSubrutaTest {

    private static final String CLAVE_4A = "SB-4A";
    private static final String CLAVE_4C = "SB-4C";

    private FakeConversacionRepository conversaciones;
    private FakeMensajeRepository mensajes;
    private FakeUsuarioRepository usuarios;
    private FakeEliminacionGrupoRepository eliminaciones;
    private FakeAsignacionTurnoRepository turnos;

    private SincronizarGrupoRutaUseCase sincronizar;
    private ValidarSubrutaChoferUseCase validar;
    private CambiarSubrutaTurnoUseCase cambiar;
    private ListarConversacionesUsuarioUseCase listar;
    private ObtenerMensajesUseCase obtenerMensajes;
    private EnviarMensajeTextoUseCase enviar;

    private UUID choferA;
    private UUID choferB;
    private UUID turnoA;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        mensajes = new FakeMensajeRepository();
        usuarios = new FakeUsuarioRepository();
        eliminaciones = new FakeEliminacionGrupoRepository();
        turnos = new FakeAsignacionTurnoRepository();

        CrearConversacionUseCase crear = new CrearConversacionUseCase(
                conversaciones, usuarios, mensajes,
                new FakeArchivoPendienteRepository(), eliminaciones,
                new MensajeSistemaService(mensajes, usuarios, conversaciones, new FakeChatNotificacion()));

        sincronizar = new SincronizarGrupoRutaUseCase(crear, turnos, usuarios, conversaciones);
        validar = new ValidarSubrutaChoferUseCase(turnos);
        cambiar = new CambiarSubrutaTurnoUseCase(turnos, sincronizar);
        listar = new ListarConversacionesUsuarioUseCase(conversaciones, mensajes, eliminaciones);
        obtenerMensajes = new ObtenerMensajesUseCase(mensajes, conversaciones);
        enviar = new EnviarMensajeTextoUseCase(mensajes, conversaciones, usuarios);

        choferA = usuarios.agregar("Conductor A");
        choferB = usuarios.agregar("Conductor B");
        turnoA = turnos.agregar(choferA, CLAVE_4A, "4A: Ciudad de Dios → Terminal");
        turnos.agregar(choferB, CLAVE_4A, "4A: Ciudad de Dios → Terminal");
    }

    @Test
    void dosConductoresEnLaMismaSubrutaCompartenGrupoYMensajes() {
        sincronizar.paraChofer(choferA);
        sincronizar.paraChofer(choferB);

        ConversacionResponseDto grupoA = unicoGrupo(choferA);
        ConversacionResponseDto grupoB = unicoGrupo(choferB);

        assertEquals(grupoA.getId(), grupoB.getId(), "Deben estar en el MISMO grupo de 4A");
        assertEquals("GRUPO_RUTA", grupoA.getTipoConversacion());
        assertEquals(CLAVE_4A, grupoA.getClaveRuta());
        assertEquals(2, grupoA.getTotalParticipantes());
        // El creador SIEMPRE es ADMIN, incluso al reingresar a un grupo existente.
        assertEquals("ADMIN", conversaciones.obtenerRol(grupoA.getId(), choferA));
        assertEquals("MIEMBRO", conversaciones.obtenerRol(grupoA.getId(), choferB));

        enviar.ejecutar(grupoA.getId(), EnviarMensajeTextoRequestDto.builder()
                .contenidoTexto("mensaje de la ruta 4A")
                .emisorId(choferA)
                .build());

        // El grupo incluye avisos de sistema al entrar cada conductor: buscamos
        // el mensaje del conductor, no un total exacto.
        org.junit.jupiter.api.Assertions.assertTrue(
                obtenerMensajes.ejecutar(grupoA.getId(), choferB).stream()
                        .anyMatch(m -> "mensaje de la ruta 4A".equals(m.getContenidoTexto())),
                "El conductor B debe ver el mensaje del conductor A en la misma subruta");
    }

    @Test
    void alCambiarDeSubrutaElGrupoYMensajesAnterioresDesaparecenParaQuienSeVa() {
        sincronizar.paraChofer(choferA);
        sincronizar.paraChofer(choferB);
        ConversacionResponseDto grupo4A = unicoGrupo(choferA);

        enviar.ejecutar(grupo4A.getId(), EnviarMensajeTextoRequestDto.builder()
                .contenidoTexto("privado de 4A")
                .emisorId(choferA)
                .build());

        // El conductor A pasa a 4C: el backend recalcula su membresía.
        cambiar.ejecutar(turnoA, CLAVE_4C, choferA, false);

        ConversacionResponseDto grupo4C = unicoGrupo(choferA);
        assertEquals(CLAVE_4C, grupo4C.getClaveRuta());
        assertNotEquals(grupo4A.getId(), grupo4C.getId(), "4C debe ser un grupo distinto");

        // A ya no participa en 4A: no ve el grupo ni sus mensajes.
        assertThrows(AccesoDenegadoException.class,
                () -> obtenerMensajes.ejecutar(grupo4A.getId(), choferA));
        assertEquals(List.of(grupo4C.getId()),
                listar.ejecutar(choferA).stream().map(ConversacionResponseDto::getId).toList());

        // B sigue en 4A con su grupo y su mensaje intactos.
        ConversacionResponseDto sigueEn4A = unicoGrupo(choferB);
        assertEquals(grupo4A.getId(), sigueEn4A.getId());
        org.junit.jupiter.api.Assertions.assertTrue(
                obtenerMensajes.ejecutar(grupo4A.getId(), choferB).stream()
                        .anyMatch(m -> "privado de 4A".equals(m.getContenidoTexto())),
                "B conserva el mensaje de 4A aunque A haya cambiado de subruta");
    }

    @Test
    void elJefeDelPanelEsAdminDelGrupoDeSuSubruta() {
        UUID jefe = usuarios.save(UsuarioModel.builder()
                .id(UUID.randomUUID())
                .nombre("Jefe de ruta")
                .rol("JEFE_RUTA")
                .activo(true)
                .subrutaCodigo(CLAVE_4A)
                .subrutaNombre("4A: Ciudad de Dios → Terminal")
                .build()).getId();

        sincronizar.sincronizarJefes();

        var grupos = listar.ejecutar(jefe);
        assertEquals(1, grupos.size());
        assertEquals(CLAVE_4A, grupos.get(0).getClaveRuta());
        assertEquals("ADMIN", conversaciones.obtenerRol(grupos.get(0).getId(), jefe));
    }

    @Test
    void unConductorNoPuedeUnirseAlGrupoDeOtraSubruta() {
        assertThrows(AccesoDenegadoException.class,
                () -> validar.ejecutar(choferA, CLAVE_4C));
    }

    @Test
    void unCodigoDeLineaHeredadoNoRompeLaSincronizacionDelTurno() {
        UUID choferC = usuarios.agregar("Conductor C");
        turnos.agregar(choferC, "C2", "Línea C2");

        // No lanza y no crea un grupo de ruta inválido.
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> sincronizar.paraChofer(choferC));
        assertEquals(0, listar.ejecutar(choferC).size());
    }

    private ConversacionResponseDto unicoGrupo(UUID choferId) {
        List<ConversacionResponseDto> grupos = listar.ejecutar(choferId);
        assertEquals(1, grupos.size(), "El chofer solo debe ver el grupo de su subruta");
        return grupos.get(0);
    }
}
