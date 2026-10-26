package com.andescode.gowaykinest.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "asignacion_turno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id", nullable = false)
    private Usuario chofer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(nullable = false)
    @Builder.Default
    private LocalDate fecha = LocalDate.now();

    @Column(name = "hora_inicio", nullable = false)
    private OffsetDateTime horaInicio;

    @Column(name = "hora_fin")
    private OffsetDateTime horaFin;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "EN_RUTA";

    @Column(name = "porcentaje_cumplimiento", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeCumplimiento = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
