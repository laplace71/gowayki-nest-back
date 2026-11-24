package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.CambioSubrutaRequestDto;
import com.gowayki.nest.application.dto.RegistroSolicitudRequestDto;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.model.SolicitudRegistroModel;
import com.gowayki.nest.domain.model.UsuarioModel;
import com.gowayki.nest.domain.repository.SolicitudRegistroPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Subruta;
import com.gowayki.nest.infrastructure.persistence.repository.SpringDataSubrutaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Crea solicitudes desde la app. El alta/cambio solo se aplica cuando el jefe aprueba en el panel. */
@Service
public class CrearSolicitudesUseCase {

    private final SolicitudRegistroPort solicitudPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SpringDataSubrutaRepository subrutaRepository;
    private final PasswordEncoder passwordEncoder;

    public CrearSolicitudesUseCase(SolicitudRegistroPort solicitudPort,
                                   UsuarioRepositoryPort usuarioRepositoryPort,
                                   SpringDataSubrutaRepository subrutaRepository,
                                   PasswordEncoder passwordEncoder) {
        this.solicitudPort = solicitudPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.subrutaRepository = subrutaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SolicitudRegistroModel registroChofer(RegistroSolicitudRequestDto request) {
        if (request == null) throw new IllegalArgumentException("Faltan datos de registro.");
        String dni = request.getDni() == null ? "" : request.getDni().trim();
        String pin = request.getPin() == null ? "" : request.getPin().trim();
        String nombre = request.getNombre() == null ? "" : request.getNombre().trim();
        String telefono = request.getTelefono() == null ? "" : request.getTelefono().trim();
        if (dni.length() != 8) throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
        if (telefono.length() != 9) throw new IllegalArgumentException("El teléfono debe tener 9 dígitos.");
        if (pin.length() < 4) throw new IllegalArgumentException("El PIN debe tener al menos 4 dígitos.");
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (request.getSubrutaId() == null) throw new IllegalArgumentException("Selecciona una subruta.");

        Subruta subruta = subrutaRepository.findById(request.getSubrutaId())
                .orElseThrow(() -> new IllegalArgumentException("La subruta no existe."));
        // Una cuenta dada de baja conserva su fila para no destruir el
        // historial. Si ese DNI está inactivo, la solicitud se marca con su id
        // para que el panel la reactive al aprobar, en vez de crear otra.
        var existenteOpt = usuarioRepositoryPort.findByDni(dni);
        if (existenteOpt.isPresent() && Boolean.TRUE.equals(existenteOpt.get().getActivo())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese DNI.");
        }
        UUID usuarioReactivar = existenteOpt.map(UsuarioModel::getId).orElse(null);

        OffsetDateTime ahora = OffsetDateTime.now();
        return solicitudPort.guardar(SolicitudRegistroModel.builder()
                .tipo("CHOFER")
                .dni(dni)
                .usuarioId(usuarioReactivar)
                .nombreCompleto(nombre)
                .telefono(telefono)
                .email(request.getEmail() == null ? null : request.getEmail().trim().toLowerCase())
                .rutaTrabajo(subruta.getNombre())
                .lineaSolicitadaId(request.getLineaId() != null ? request.getLineaId() : lineaDe(subruta))
                .subrutaSolicitadaId(subruta.getId())
                .pinHash(passwordEncoder.encode(pin))
                .estado("PENDIENTE")
                .creadoEn(ahora)
                .actualizadoEn(ahora)
                .build());
    }

    @Transactional
    public SolicitudRegistroModel cambioSubruta(UUID choferId, CambioSubrutaRequestDto request) {
        if (choferId == null) throw new AccesoDenegadoException("Se requiere un token válido.");
        if (request == null || request.getSubrutaId() == null) {
            throw new IllegalArgumentException("Selecciona la subruta de destino.");
        }
        if (usuarioRepositoryPort.findById(choferId).isEmpty()) {
            throw new IllegalArgumentException("El chofer no existe.");
        }
        Subruta subruta = subrutaRepository.findById(request.getSubrutaId())
                .orElseThrow(() -> new IllegalArgumentException("La subruta no existe."));

        OffsetDateTime ahora = OffsetDateTime.now();
        return solicitudPort.guardar(SolicitudRegistroModel.builder()
                .tipo("CAMBIO_RUTA")
                .dni("")
                .nombreCompleto("")
                .usuarioId(choferId)
                .rutaTrabajo(subruta.getNombre())
                .lineaSolicitadaId(lineaDe(subruta))
                .subrutaSolicitadaId(subruta.getId())
                .estado("PENDIENTE")
                .creadoEn(ahora)
                .actualizadoEn(ahora)
                .build());
    }

    private Integer lineaDe(Subruta subruta) {
        return subruta.getLinea() == null ? null : subruta.getLinea().getId();
    }
}
