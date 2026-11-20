package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100)
    private String titulo;

    @Column(name = "es_grupo", nullable = false)
    @Builder.Default
    private Boolean esGrupo = false;

    /** PRIVADA, GRUPO o GRUPO_RUTA. Sustituye a deducirlo del título. */
    @Column(name = "tipo_conversacion", nullable = false, length = 20)
    @Builder.Default
    private String tipoConversacion = "PRIVADA";

    /** Subruta a la que pertenece un GRUPO_RUTA; null en el resto. */
    @Column(name = "clave_ruta", length = 150)
    private String claveRuta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Usuario createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Soft delete: null mientras el grupo está vivo; fecha de marcado si se eliminó. */
    @Column(name = "eliminado_en")
    private OffsetDateTime eliminadoEn;
}
