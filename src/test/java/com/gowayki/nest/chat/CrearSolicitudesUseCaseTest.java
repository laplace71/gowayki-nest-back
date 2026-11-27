package com.gowayki.nest.chat;

import com.gowayki.nest.application.dto.RegistroSolicitudRequestDto;
import com.gowayki.nest.application.usecase.CrearSolicitudesUseCase;
import com.gowayki.nest.domain.model.SolicitudRegistroModel;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.SolicitudRegistroPort;
import com.gowayki.nest.infrastructure.persistence.entity.Subruta;
import com.gowayki.nest.infrastructure.persistence.repository.SpringDataSubrutaRepository;
import com.gowayki.nest.support.FakeUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CrearSolicitudesUseCaseTest {

    @Test
    void registroConDniInactivoMarcaLaCuentaARreactivar() {
        UUID inactivoId = UUID.randomUUID();
        FakeUsuarioRepository usuarios = new FakeUsuarioRepository();
        usuarios.save(UsuarioModel.builder()
                .id(inactivoId)
                .dni("12345678")
                .activo(false)
                .build());

        SolicitudRegistroModel[] guardada = new SolicitudRegistroModel[1];
        SolicitudRegistroPort solicitudes = model -> {
            guardada[0] = model;
            return model;
        };

        SpringDataSubrutaRepository subrutas = mock(SpringDataSubrutaRepository.class);
        when(subrutas.findById(5)).thenReturn(Optional.of(
                Subruta.builder().id(5).codigo("SB-17620010").nombre("4A: Ciudad de Dios → Terminal").build()));

        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("hash");

        CrearSolicitudesUseCase useCase =
                new CrearSolicitudesUseCase(solicitudes, usuarios, subrutas, encoder);

        var solicitud = useCase.registroChofer(RegistroSolicitudRequestDto.builder()
                .nombre("Juan")
                .dni("12345678")
                .telefono("987654321")
                .pin("1234")
                .subrutaId(5)
                .build());

        assertEquals(inactivoId, solicitud.getUsuarioId());
        assertEquals(inactivoId, guardada[0].getUsuarioId());
    }
}
