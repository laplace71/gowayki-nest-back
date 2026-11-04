package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import com.gowayki.nest.infrastructure.persistence.entity.Mensaje;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import com.gowayki.nest.infrastructure.persistence.mapper.MensajeMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MensajePersistenceAdapter implements MensajeRepositoryPort {

    private final SpringDataMensajeRepository mensajeRepository;
    private final SpringDataConversacionRepository conversacionRepository;
    private final SpringDataUsuarioRepository usuarioRepository;
    private final MensajeMapper mensajeMapper;

    public MensajePersistenceAdapter(SpringDataMensajeRepository mensajeRepository,
                                    SpringDataConversacionRepository conversacionRepository,
                                    SpringDataUsuarioRepository usuarioRepository,
                                    MensajeMapper mensajeMapper) {
        this.mensajeRepository = mensajeRepository;
        this.conversacionRepository = conversacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.mensajeMapper = mensajeMapper;
    }

    @Override
    @Transactional
    public MensajeModel save(MensajeModel model) {
        Conversacion conversacion = conversacionRepository.findById(model.getConversacionId())
                .orElseThrow(() -> new IllegalArgumentException("Conversación no encontrada."));

        Usuario emisor = usuarioRepository.findById(model.getEmisorId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario emisor no encontrado."));

        Mensaje entity = mensajeMapper.toEntity(model, conversacion, emisor);
        Mensaje guardado = mensajeRepository.save(entity);
        return mensajeMapper.toDomain(guardado);
    }

    @Override
    public List<MensajeModel> findByConversacionId(UUID conversacionId) {
        return mensajeRepository.findByConversacionIdOrderByCreatedAtAsc(conversacionId).stream()
                .map(mensajeMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MensajeModel> findUltimoMensaje(UUID conversacionId) {
        return mensajeRepository.findTopByConversacionIdOrderByCreatedAtDesc(conversacionId)
                .map(mensajeMapper::toDomain);
    }

    @Override
    public int contarMensajes(UUID conversacionId) {
        return mensajeRepository.countByConversacionId(conversacionId);
    }
}
