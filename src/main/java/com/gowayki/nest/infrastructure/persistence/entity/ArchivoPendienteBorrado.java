package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "archivo_pendiente_borrado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoPendienteBorrado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String ruta;

    @Column(length = 40)
    private String motivo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Integer intentos = 0;

    @Column(name = "ultimo_intento_en")
    private OffsetDateTime ultimoIntentoEn;

    @Column(name = "ultimo_error", length = 500)
    private String ultimoError;
}
