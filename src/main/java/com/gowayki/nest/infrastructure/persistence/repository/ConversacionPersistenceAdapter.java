package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipante;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipanteId;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionUsuarioEstado;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionUsuarioEstadoId;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import com.gowayki.nest.infrastructure.persistence.mapper.ConversacionMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConversacionPersistenceAdapter implements ConversacionRepositoryPort {

    private final SpringDataConversacionRepository conversacionRepository;
    private final SpringDataConversacionParticipanteRepository participanteRepository;
    private final SpringDataConversacionUsuarioEstadoRepository estadoRepository;
    private final SpringDataUsuarioRepository usuarioRepository;
    private final ConversacionMapper conversacionMapper;

    public ConversacionPersistenceAdapter(SpringDataConversacionRepository conversacionRepository,
                                         SpringDataConversacionParticipanteRepository participanteRepository,
                                         SpringDataConversacionUsuarioEstadoRepository estadoRepository,
                                         SpringDataUsuarioRepository usuarioRepository,
                                         ConversacionMapper conversacionMapper) {
        this.conversacionRepository = conversacionRepository;
        this.participanteRepository = participanteRepository;
        this.estadoRepository = estadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.conversacionMapper = conversacionMapper;
    }

    @Override
    @Transactional
    public ConversacionModel save(ConversacionModel model, List<UUID> participanteIds) {
        Usuario creator = null;
        if (model.getCreatedBy() != null) {
            creator = usuarioRepository.findById(model.getCreatedBy()).orElse(null);
        }

        Conversacion entity = conversacionMapper.toEntity(model, creator);
        Conversacion guardada = conversacionRepository.save(entity);

        if (participanteIds != null) {
            for (UUID pId : participanteIds) {
                Usuario user = usuarioRepository.findById(pId).orElse(null);
                if (user != null) {
                    ConversacionParticipanteId cpId = new ConversacionParticipanteId(guardada.getId(), user.getId());
                    ConversacionParticipante cp = ConversacionParticipante.builder()
                            .id(cpId)
                            .conversacion(guardada)
                            .chofer(user)
                            .rol(user.getId().equals(model.getCreatedBy()) ? "ADMIN" : "MIEMBRO")
                            .build();
                    participanteRepository.save(cp);
                }
            }
        }

        return conversacionMapper.toDomain(guardada);
    }

    @Override
    public Optional<ConversacionModel> findById(UUID id) {
        return conversacionRepository.findById(id).map(conversacionMapper::toDomain);
    }

    @Override
    public List<ConversacionModel> findByUsuarioId(UUID usuarioId) {
        return conversacionRepository.findByUsuarioId(usuarioId).stream()
                .map(conversacionMapper::toDomain)
                .toList();
    }

    @Override
    public List<ConversacionModel> findByUsuarioIdSinFiltroOcultas(UUID usuarioId) {
        return conversacionRepository.findByUsuarioIdSinFiltroOcultas(usuarioId).stream()
                .map(conversacionMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existeParticipante(UUID conversacionId, UUID usuarioId) {
        return participanteRepository.existsByIdConversacionIdAndIdChoferId(conversacionId, usuarioId);
    }

    @Override
    public Optional<ConversacionModel> findByTitulo(String titulo) {
        return conversacionRepository.findFirstByTitulo(titulo).map(conversacionMapper::toDomain);
    }

    @Override
    @Transactional
    public void agregarParticipante(UUID conversacionId, UUID usuarioId) {
        // Bloquea la conversación: sin esto, dos peticiones simultáneas pueden
        // pasar ambas la comprobación y chocar con la clave primaria.
        Conversacion conversacion = conversacionRepository.findByIdForUpdate(conversacionId).orElse(null);
        if (conversacion == null) {
            return;
        }
        if (participanteRepository.existsByIdConversacionIdAndIdChoferId(conversacionId, usuarioId)) {
            return;
        }
        Usuario user = usuarioRepository.findById(usuarioId).orElse(null);
        if (user != null) {
            ConversacionParticipanteId cpId = new ConversacionParticipanteId(conversacionId, usuarioId);
            ConversacionParticipante cp = ConversacionParticipante.builder()
                    .id(cpId)
                    .conversacion(conversacion)
                    .chofer(user)
                    .rol("MIEMBRO")
                    .build();
            participanteRepository.save(cp);
            // Si había ocultado este grupo, al volver a participar debe verlo.
            limpiarEstadoUsuario(conversacionId, usuarioId);
        }
    }

    @Override
    public int contarParticipantes(UUID conversacionId) {
        return (int) participanteRepository.countByIdConversacionId(conversacionId);
    }

    @Override
    public boolean esAdministrador(UUID conversacionId, UUID usuarioId) {
        return participanteRepository
                .findById(new ConversacionParticipanteId(conversacionId, usuarioId))
                .map(participante -> "ADMIN".equals(participante.getRol()))
                .orElse(false);
    }

    @Override
    public String obtenerRol(UUID conversacionId, UUID usuarioId) {
        return participanteRepository
                .findById(new ConversacionParticipanteId(conversacionId, usuarioId))
                .map(ConversacionParticipante::getRol)
                .orElse(null);
    }

    @Override
    @Transactional
    public void quitarParticipante(UUID conversacionId, UUID usuarioId) {
        if (!participanteRepository.existsByIdConversacionIdAndIdChoferId(conversacionId, usuarioId)) {
            throw new IllegalArgumentException("No formas parte de esta conversación.");
        }
        participanteRepository.deleteById(new ConversacionParticipanteId(conversacionId, usuarioId));
    }

    @Override
    @Transactional
    public void cambiarRol(UUID conversacionId, UUID usuarioId, String nuevoRol) {
        ConversacionParticipante participante = participanteRepository
                .findById(new ConversacionParticipanteId(conversacionId, usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("El usuario no forma parte de esta conversación."));
        participante.setRol(nuevoRol);
        participanteRepository.save(participante);
    }

    @Override
    public java.util.List<com.gowayki.nest.domain.model.ParticipanteConversacion> listarParticipantes(UUID conversacionId) {
        return participanteRepository.findAllByIdConversacionId(conversacionId).stream()
                .map(p -> com.gowayki.nest.domain.model.ParticipanteConversacion.builder()
                        .userId(p.getChofer() != null ? p.getChofer().getId() : null)
                        .nombre(p.getChofer() != null ? p.getChofer().getNombre() : null)
                        .rol(p.getRol())
                        .fotoUrl(p.getChofer() != null ? p.getChofer().getFotoUrl() : null)
                        .isOnline(p.getChofer() != null ? p.getChofer().getIsOnline() : null)
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void eliminar(UUID conversacionId) {
        // Participantes y mensajes se van solos: las dos FK son ON DELETE CASCADE.
        conversacionRepository.deleteById(conversacionId);
    }

    @Override
    @Transactional
    public void ocultarParaUsuario(UUID conversacionId, UUID usuarioId) {
        ConversacionUsuarioEstadoId id = new ConversacionUsuarioEstadoId(conversacionId, usuarioId);
        ConversacionUsuarioEstado estado = estadoRepository.findById(id)
                .orElseGet(() -> ConversacionUsuarioEstado.builder().id(id).build());
        estado.setOculta(true);
        estado.setOcultaEn(java.time.OffsetDateTime.now());
        estadoRepository.save(estado);
    }

    @Override
    @Transactional
    public void mostrarParaUsuario(UUID conversacionId, UUID usuarioId) {
        estadoRepository.findById(new ConversacionUsuarioEstadoId(conversacionId, usuarioId))
                .ifPresent(estado -> {
                    estado.setOculta(false);
                    estado.setOcultaEn(null);
                    estadoRepository.save(estado);
                });
    }

    @Override
    @Transactional
    public void limpiarEstadoUsuario(UUID conversacionId, UUID usuarioId) {
        estadoRepository.deleteById(new ConversacionUsuarioEstadoId(conversacionId, usuarioId));
    }

    @Override
    @Transactional
    public Optional<ConversacionModel> bloquearParaActualizar(UUID conversacionId) {
        return conversacionRepository.findByIdForUpdate(conversacionId).map(conversacionMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<UUID> promoverAdministradorSiHaceFalta(UUID conversacionId) {
        List<ConversacionParticipante> participantes =
                participanteRepository.findAllByIdConversacionId(conversacionId);

        if (participantes.isEmpty()) {
            return Optional.empty();
        }
        if (participantes.stream().anyMatch(p -> "ADMIN".equals(p.getRol()))) {
            return Optional.empty();
        }

        // Determinista: el más antiguo y, a igualdad de fecha, el UUID menor.
        ConversacionParticipante elegido = participantes.stream()
                .min(Comparator
                        .comparing(ConversacionParticipante::getAgregadoEn,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(p -> p.getId().getChoferId()))
                .orElseThrow();

        elegido.setRol("ADMIN");
        participanteRepository.save(elegido);
        return Optional.of(elegido.getId().getChoferId());
    }
}

