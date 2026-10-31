package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.OffsetDateTime;

@Entity
@Table(name = "punto_control")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linea_id", nullable = false)
    private Linea linea;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Integer orden;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point ubicacion;

    @Column(name = "radio_tolerancia_metros", nullable = false)
    @Builder.Default
    private Integer radioToleranciaMetros = 50;

    @Column(name = "tolerancia_minutos", nullable = false)
    @Builder.Default
    private Integer toleranciaMinutos = 2;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
