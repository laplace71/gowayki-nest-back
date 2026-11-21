package com.gowayki.nest.security;

import com.gowayki.nest.domain.exception.NoAutenticadoException;
import com.gowayki.nest.infrastructure.rest.ChatController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * La identidad de quien borra SIEMPRE sale del JWT. Las peticiones sin
 * autenticación se rechazan antes de tocar cualquier repositorio.
 */
class ChatEndpointIdentityTest {

    @SuppressWarnings("unchecked")
    private ChatController controllerSinDependencias() throws Exception {
        Constructor<ChatController> constructor =
                (Constructor<ChatController>) ChatController.class.getDeclaredConstructors()[0];
        Object[] argumentos = new Object[constructor.getParameterCount()]; // todos null
        return constructor.newInstance(argumentos);
    }

    @Test
    void ocultarConversacionSinTokenNoHaceNada() throws Exception {
        ChatController controller = controllerSinDependencias();
        assertThrows(NoAutenticadoException.class,
                () -> controller.ocultarConversacion(UUID.randomUUID(), null));
    }

    @Test
    void ocultarMensajeSinTokenNoHaceNada() throws Exception {
        ChatController controller = controllerSinDependencias();
        assertThrows(NoAutenticadoException.class,
                () -> controller.ocultarMensaje(UUID.randomUUID(), UUID.randomUUID(), null));
    }

    @Test
    void eliminarMensajeParaTodosSinTokenNoHaceNada() throws Exception {
        ChatController controller = controllerSinDependencias();
        assertThrows(NoAutenticadoException.class,
                () -> controller.eliminarMensajeParaTodos(UUID.randomUUID(), UUID.randomUUID(), null));
    }
}
