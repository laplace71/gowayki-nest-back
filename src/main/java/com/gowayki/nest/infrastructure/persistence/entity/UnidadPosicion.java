package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "unidad_posicion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnidadPosicion {

    @Id
    @Column(name = "unidad_id")
    private Integer unidadId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point posicion;

    @Column(name = "velocidad_kmh", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal velocidadKmh = BigDecimal.ZERO;

    @Column(name = "rumbo_grados", nullable = false)
    @Builder.Default
    private Integer rumboGrados = 0;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
