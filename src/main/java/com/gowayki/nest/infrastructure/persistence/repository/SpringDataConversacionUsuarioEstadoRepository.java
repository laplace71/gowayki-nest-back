package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.ConversacionUsuarioEstado;
import com.gowayki.nest.infrastructure.persistence.entity.ConversacionUsuarioEstadoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataConversacionUsuarioEstadoRepository
        extends JpaRepository<ConversacionUsuarioEstado, ConversacionUsuarioEstadoId> {
}
