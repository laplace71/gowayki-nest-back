package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitud_registro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, length = 8)
    private String dni;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(length = 9)
    private String telefono;

    @Column(length = 120)
    private String email;

    @Column(name = "placa_numero", length = 10)
    private String placaNumero;

    @Column(name = "ruta_trabajo", length = 100)
    private String rutaTrabajo;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "linea_solicitada_id")
    private Integer lineaSolicitadaId;

    @Column(name = "subruta_solicitada_id")
    private Integer subrutaSolicitadaId;

    @Column(name = "pin_hash", length = 255)
    private String pinHash;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "usuario_asignado", length = 50)
    private String usuarioAsignado;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;
}
