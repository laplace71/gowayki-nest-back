package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversacion_eliminacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EliminacionGrupo {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String CANCELADA = "CANCELADA";
    public static final String EJECUTADA = "EJECUTADA";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conversacion_id")
    private UUID conversacionId;

    @Column(name = "titulo_snapshot", length = 100)
    private String tituloSnapshot;

    @Column(name = "solicitado_por", nullable = false)
    private UUID solicitadoPor;

    @Column(name = "solicitado_en", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime solicitadoEn = OffsetDateTime.now();

    @Column(name = "ejecutar_despues", nullable = false)
    private OffsetDateTime ejecutarDespues;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = PENDIENTE;

    @Column(name = "cancelada_por")
    private UUID canceladaPor;

    @Column(name = "cancelada_en")
    private OffsetDateTime canceladaEn;

    @Column(name = "ejecutada_en")
    private OffsetDateTime ejecutadaEn;
}
