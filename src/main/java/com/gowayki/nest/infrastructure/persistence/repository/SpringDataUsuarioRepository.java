package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByDni(String dni);
    Optional<Usuario> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByTelefono(String telefono);

    List<Usuario> findByActivoTrue();

    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = :intentos, u.bloqueadoHasta = :bloqueado WHERE u.id = :id")
    void registrarIntentoFallido(@Param("id") UUID id, @Param("intentos") int intentos,
                                 @Param("bloqueado") OffsetDateTime bloqueado);

    @Modifying
    @Query("UPDATE Usuario u SET u.intentosFallidos = 0, u.bloqueadoHasta = null, u.isOnline = true WHERE u.id = :id")
    void marcarLoginExitoso(@Param("id") UUID id);

    @Query(value = "SELECT u.id, s.codigo, s.nombre FROM usuario u " +
            "JOIN usuario_linea ul ON ul.usuario_id = u.id " +
            "JOIN subruta s ON s.id = ul.subruta_id " +
            "WHERE u.rol = 'JEFE_RUTA' AND u.activo = true AND ul.subruta_id IS NOT NULL",
            nativeQuery = true)
    List<Object[]> findJefesConSubruta();

    @Query("SELECT u.activo FROM Usuario u WHERE u.id = :id")
    Boolean estaActivo(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Usuario u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :id")
    void incrementarTokenVersion(@Param("id") UUID id);
}

