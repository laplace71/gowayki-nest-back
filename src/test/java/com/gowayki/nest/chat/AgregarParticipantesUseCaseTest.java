package com.gowayki.nest.chat;

import com.gowayki.nest.application.usecase.AgregarParticipantesUseCase;
import com.gowayki.nest.application.usecase.MensajeSistemaService;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.model.ConversacionModel;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Las invitaciones afectan al grupo indicado por id, no a otro parecido. */
class AgregarParticipantesUseCaseTest {

    private FakeConversacionRepository conversaciones;
    private AgregarParticipantesUseCase usar;
    private UUID admin;
    private UUID otro;
    private UUID invitado;

    @BeforeEach
    void preparar() {
        conversaciones = new FakeConversacionRepository();
        FakeMensajeRepository mensajes = new FakeMensajeRepository();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        usar = new AgregarParticipantesUseCase(
                conversaciones, new FakeEliminacionGrupoRepository(),
                new MensajeSistemaService(mensajes, usuarios, conversaciones, new FakeChatNotificacion()));
        admin = UUID.randomUUID();
        otro = UUID.randomUUID();
        invitado = UUID.randomUUID();
    }

    private UUID crearGrupo(String titulo, UUID... miembros) {
        return conversaciones.agregar(ConversacionModel.builder()
                .titulo(titulo)
                .esGrupo(true)
                .tipoConversacion("GRUPO")
                .createdBy(admin)
                .createdAt(OffsetDateTime.now())
                .build(), miembros);
    }

    @Test
    void invitaAlGrupoIndicado() {
        UUID objetivo = crearGrupo("Operativos", admin);
        UUID otroGrupo = crearGrupo("Operativos", otro);

        usar.ejecutar(objetivo, admin, List.of(invitado));

        assertTrue(conversaciones.existeParticipante(objetivo, invitado));
        assertFalse(conversaciones.existeParticipante(otroGrupo, invitado));
    }

    @Test
    void unMiembroNormalNoPuedeInvitar() {
        UUID objetivo = crearGrupo("Operativos", admin, otro);
        assertThrows(AccesoDenegadoException.class,
                () -> usar.ejecutar(objetivo, otro, List.of(invitado)));
        assertFalse(conversaciones.existeParticipante(objetivo, invitado));
    }
}
