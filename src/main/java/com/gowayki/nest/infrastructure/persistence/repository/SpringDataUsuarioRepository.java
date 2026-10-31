package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByDni(String dni);
    Optional<Usuario> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByTelefono(String telefono);
}
