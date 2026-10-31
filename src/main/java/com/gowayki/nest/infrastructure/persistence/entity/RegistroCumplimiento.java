package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "registro_cumplimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroCumplimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionTurno asignacionTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_control_id", nullable = false)
    private PuntoControl puntoControl;

    @Column(name = "hora_programada", nullable = false)
    private OffsetDateTime horaProgramada;

    @Column(name = "hora_llegada", nullable = false)
    private OffsetDateTime horaLlegada;

    @Column(name = "diferencia_minutos", nullable = false)
    private Integer diferenciaMinutos;

    @Column(nullable = false, length = 15)
    private String estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
