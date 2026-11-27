package com.gowayki.nest.chat;

import com.gowayki.nest.application.dto.CrearConversacionRequestDto;
import com.gowayki.nest.application.dto.EnviarMensajeTextoRequestDto;
import com.gowayki.nest.application.usecase.AsignarRolParticipanteUseCase;
import com.gowayki.nest.application.usecase.CrearConversacionUseCase;
import com.gowayki.nest.application.usecase.EnviarMensajeTextoUseCase;
import com.gowayki.nest.application.usecase.MensajeSistemaService;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.ConflictoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.support.FakeArchivoPendienteRepository;
import com.gowayki.nest.support.FakeChatNotificacion;
import com.gowayki.nest.support.FakeConversacionRepository;
import com.gowayki.nest.support.FakeEliminacionGrupoRepository;
import com.gowayki.nest.support.FakeMensajeRepository;
import com.gowayki.nest.support.FakeUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** Regresiones de los hallazgos de acceso y exposición de datos. */
class SeguridadAccesoTest {

    private FakeConversacionRepository conversaciones;
    private FakeUsuarioRepository usuarios;
    private UUID ana;
    private UUID bpf;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        usuarios = new FakeUsuarioRepository();
        ana = usuarios.agregar("Ana");
        bpf = usuarios.agregar("Beto");
    }

    private CrearConversacionUseCase crear() {
        FakeMensajeRepository mensajes = new FakeMensajeRepository();
        return new CrearConversacionUseCase(
                conversaciones, usuarios, mensajes,
                new FakeArchivoPendienteRepository(), new FakeEliminacionGrupoRepository(),
                new MensajeSistemaService(mensajes, usuarios, conversaciones, new FakeChatNotificacion()));
    }

    @Test
    void unTituloDeChatPrivadoNoPermiteUnirseAlGrupoDeRuta() {
        conversaciones.agregar(ConversacionModel.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(false)
                .tipoConversacion("PRIVADA")
                .createdBy(bpf)
                .createdAt(OffsetDateTime.now())
                .build(), bpf);

        CrearConversacionRequestDto peticion = CrearConversacionRequestDto.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .tipoConversacion("GRUPO_RUTA")
                .claveRuta("Línea 7 · Central")
                .createdBy(ana)
                .participanteIds(List.of(ana))
                .build();

        assertThrows(AccesoDenegadoException.class, () -> crear().ejecutar(peticion));
    }

    @Test
    void crearGrupoDeRutaNoAgargaAOtrosConductores() {
        crear().ejecutar(CrearConversacionRequestDto.builder()
                .titulo("GRUPO · Línea 7 · Central")
                .esGrupo(true)
                .tipoConversacion("GRUPO_RUTA")
                .claveRuta("Línea 7 · Central")
                .createdBy(ana)
                .participanteIds(List.of(ana, bpf))
                .build());

        UUID id = conversaciones.conversaciones.values().iterator().next().getId();
        org.junit.jupiter.api.Assertions.assertFalse(
                conversaciones.existeParticipante(id, bpf),
                "Beto no debió ser agregado ni sacado de su ruta");
    }

    @Test
    void elCreadorQueSalioNoPuedeCambiarRoles() {
        UUID id = conversaciones.agregar(ConversacionModel.builder()
                .titulo("Grupo")
                .esGrupo(true)
                .tipoConversacion("GRUPO")
                .createdBy(ana)
                .createdAt(OffsetDateTime.now())
                .build(), ana, bpf);
        conversaciones.quitarParticipante(id, ana);

        AsignarRolParticipanteUseCase asignar = new AsignarRolParticipanteUseCase(conversaciones);
        assertThrows(AccesoDenegadoException.class,
                () -> asignar.ejecutar(id, ana, bpf, "ADMIN"));
    }

    @Test
    void unGrupoPendienteDeEliminacionNoAceptaMensajes() {
        UUID id = conversaciones.agregar(ConversacionModel.builder()
                .titulo("Grupo")
                .esGrupo(true)
                .tipoConversacion("GRUPO")
                .createdBy(ana)
                .createdAt(OffsetDateTime.now())
                .eliminadoEn(OffsetDateTime.now())
                .build(), ana);

        EnviarMensajeTextoUseCase enviar = new EnviarMensajeTextoUseCase(
                new FakeMensajeRepository(), conversaciones, usuarios);

        assertThrows(ConflictoException.class,
                () -> enviar.ejecutar(id, EnviarMensajeTextoRequestDto.builder()
                        .contenidoTexto("hola")
                        .emisorId(ana)
                        .build()));
    }
}
