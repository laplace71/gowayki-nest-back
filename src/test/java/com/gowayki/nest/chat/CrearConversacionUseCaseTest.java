package com.gowayki.nest.chat;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.application.dto.CrearConversacionRequestDto;
import com.gowayki.nest.application.usecase.CrearConversacionUseCase;
import com.gowayki.nest.domain.exception.ConflictoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeConversacionRepository;
import com.gowayki.nest.support.FakeEliminacionGrupoRepository;
import com.gowayki.nest.support.FakeMensajeRepository;
import com.gowayki.nest.support.FakeUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** El tipo de grupo se decide por sus campos, no por el texto del título. */
class CrearConversacionUseCaseTest {

    private FakeConversacionRepository conversaciones;
    private FakeEliminacionGrupoRepository eliminaciones;
    private FakeUsuarioRepository usuarios;
    private CrearConversacionUseCase crear;

    private UUID ana;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        eliminaciones = new FakeEliminacionGrupoRepository();
        usuarios = new FakeUsuarioRepository();
        crear = new CrearConversacionUseCase(
                conversaciones, usuarios, new FakeMensajeRepository(),
                new FakeArchivoPendienteRepository(), eliminaciones);

        ana = usuarios.agregar("Ana");
    }

    @Test
    void elGrupoDeRutaSeReconocePorLaClaveAunqueElTituloNoLlevePrefijo() {
        ConversacionResponseDto creada = crear.ejecutar(CrearConversacionRequestDto.builder()
                .titulo("Central")
                .esGrupo(true)
                .tipoConversacion("GRUPO_RUTA")
                .claveRuta("Línea 7 · Central")
                .createdBy(ana)
                .participanteIds(List.of(ana))
                .build());

        assertEquals("GRUPO_RUTA", creada.getTipoConversacion());
        assertEquals("Línea 7 · Central", creada.getClaveRuta());

        // La segunda vez se reutiliza el mismo grupo.
        ConversacionResponseDto otraVez = crear.ejecutar(CrearConversacionRequestDto.builder()
                .titulo("Otro título cualquiera")
                .esGrupo(true)
                .tipoConversacion("GRUPO_RUTA")
                .claveRuta("Línea 7 · Central")
                .createdBy(ana)
                .participanteIds(List.of(ana))
                .build());

        assertEquals(creada.getId(), otraVez.getId());
        assertEquals(1, conversaciones.conversaciones.size());
    }

    @Test
    void unaAppAntiguaSinCamposNuevosSigueFuncionandoPorElTitulo() {
        ConversacionResponseDto creada = crear.ejecutar(CrearConversacionRequestDto.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .createdBy(ana)
                .participanteIds(List.of(ana))
                .build());

        assertEquals("GRUPO_RUTA", creada.getTipoConversacion());
        assertEquals("Línea 7 · Central", creada.getClaveRuta());
    }

    @Test
    void unGrupoNormalNoSeConfundeConUnGrupoDeRuta() {
        ConversacionResponseDto creada = crear.ejecutar(CrearConversacionRequestDto.builder()
                .titulo("Operativos")
                .esGrupo(true)
                .createdBy(ana)
                .participanteIds(List.of(ana))
                .build());

        assertEquals("GRUPO", creada.getTipoConversacion());
        assertEquals(null, creada.getClaveRuta());
    }

    @Test
    void noSePuedeEntrarAUnGrupoPendienteDeEliminacion() {
        UUID grupoId = conversaciones.agregar(ConversacionModel.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .tipoConversacion("GRUPO_RUTA")
                .claveRuta("Línea 7 · Central")
                .createdBy(ana)
                .createdAt(OffsetDateTime.now())
                .build(), ana);
        eliminaciones.agregarPendiente(grupoId, ana, OffsetDateTime.now().plusHours(72));

        CrearConversacionRequestDto peticion = CrearConversacionRequestDto.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .createdBy(ana)
                .participanteIds(List.of(ana))
                .build();

        assertThrows(ConflictoException.class, () -> crear.ejecutar(peticion));
    }
}
