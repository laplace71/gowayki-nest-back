package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipante;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionParticipanteId;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import com.gowayki.nest.infrastructure.persistence.mapper.ConversacionMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConversacionPersistenceAdapter implements ConversacionRepositoryPort {

    private final SpringDataConversacionRepository conversacionRepository;
    private final SpringDataConversacionParticipanteRepository participanteRepository;
    private final SpringDataUsuarioRepository usuarioRepository;
    private final ConversacionMapper conversacionMapper;

    public ConversacionPersistenceAdapter(SpringDataConversacionRepository conversacionRepository,
                                         SpringDataConversacionParticipanteRepository participanteRepository,
                                         SpringDataUsuarioRepository usuarioRepository,
                                         ConversacionMapper conversacionMapper) {
        this.conversacionRepository = conversacionRepository;
        this.participanteRepository = participanteRepository;
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
    public boolean existeParticipante(UUID conversacionId, UUID usuarioId) {
        return participanteRepository.existsByIdConversacionIdAndIdChoferId(conversacionId, usuarioId);
    }
}

