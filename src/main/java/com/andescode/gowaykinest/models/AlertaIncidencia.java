package com.andescode.gowaykinest.models;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerta_incidencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chofer_id", nullable = false)
    private Usuario chofer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "origen_disparo", nullable = false, length = 20)
    private String origenDisparo;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point ubicacion;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVA";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "expira_at")
    private OffsetDateTime expiraAt;
}
