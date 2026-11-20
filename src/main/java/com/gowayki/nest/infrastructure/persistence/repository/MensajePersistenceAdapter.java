package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.infrastructure.persistence.entity.Conversacion;
import com.gowayki.nest.infrastructure.persistence.entity.Mensaje;
import com.gowayki.nest.infrastructure.persistence.entity.MensajeOculto;
import com.gowayki.nest.infrastructure.persistence.entity.MensajeOcultoId;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import com.gowayki.nest.infrastructure.persistence.mapper.MensajeMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MensajePersistenceAdapter implements MensajeRepositoryPort {

    private final SpringDataMensajeRepository mensajeRepository;
    private final SpringDataMensajeOcultoRepository mensajeOcultoRepository;
    private final SpringDataConversacionRepository conversacionRepository;
    private final SpringDataUsuarioRepository usuarioRepository;
    private final MensajeMapper mensajeMapper;

    public MensajePersistenceAdapter(SpringDataMensajeRepository mensajeRepository,
                                     SpringDataMensajeOcultoRepository mensajeOcultoRepository,
                                    SpringDataConversacionRepository conversacionRepository,
                                    SpringDataUsuarioRepository usuarioRepository,
                                    MensajeMapper mensajeMapper) {
        this.mensajeRepository = mensajeRepository;
        this.mensajeOcultoRepository = mensajeOcultoRepository;
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
    public List<MensajeModel> findVisiblesByConversacionId(UUID conversacionId) {
        return mensajeRepository.findVisiblesByConversacionIdOrderByCreatedAtAsc(conversacionId).stream()
                .map(mensajeMapper::toDomain)
                .toList();
    }

    @Override
    public List<MensajeModel> findVisiblesParaUsuario(UUID conversacionId, UUID usuarioId) {
        return mensajeRepository.findVisiblesParaUsuario(conversacionId, usuarioId).stream()
                .map(mensajeMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MensajeModel> findUltimoMensaje(UUID conversacionId, UUID usuarioId) {
        return mensajeRepository.findTopVisibleParaUsuario(conversacionId, usuarioId)
                .map(mensajeMapper::toDomain);
    }

    @Override
    public int contarMensajes(UUID conversacionId, UUID usuarioId) {
        return mensajeRepository.countVisiblesParaUsuario(conversacionId, usuarioId);
    }

    @Override
    public boolean existeAudioUrl(String audioUrl) {
        return audioUrl != null && mensajeRepository.existsByAudioUrl(audioUrl);
    }

    @Override
    @Transactional
    public void marcarEliminados(UUID conversacionId, java.time.OffsetDateTime momento) {
        mensajeRepository.marcarEliminados(conversacionId, momento);
    }

    @Override
    @Transactional
    public void desmarcarEliminados(UUID conversacionId) {
        mensajeRepository.desmarcarEliminados(conversacionId);
    }

    @Override
    public Optional<MensajeModel> findById(UUID mensajeId) {
        return mensajeRepository.findById(mensajeId).map(mensajeMapper::toDomain);
    }

    @Override
    @Transactional
    public void ocultarParaUsuario(UUID mensajeId, UUID usuarioId) {
        MensajeOcultoId id = new MensajeOcultoId(mensajeId, usuarioId);
        MensajeOculto marca = mensajeOcultoRepository.findById(id)
                .orElseGet(() -> MensajeOculto.builder()
                        .id(id)
                        .ocultoEn(OffsetDateTime.now())
                        .build());
        mensajeOcultoRepository.save(marca);
    }

    @Override
    @Transactional
    public void marcarEliminado(UUID mensajeId, OffsetDateTime momento) {
        mensajeRepository.marcarEliminado(mensajeId, momento);
    }

    @Override
    @Transactional
    public void eliminarFisicamente(UUID mensajeId) {
        mensajeRepository.eliminarFisicamente(mensajeId);
    }
}

