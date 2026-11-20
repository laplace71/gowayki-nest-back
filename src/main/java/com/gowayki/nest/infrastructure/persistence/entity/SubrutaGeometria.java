package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subruta_geometria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubrutaGeometria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subruta_id", nullable = false)
    private Subruta subruta;

    @Column(nullable = false, length = 10)
    private String sentido;

    @Column(name = "puntos_json", nullable = false, columnDefinition = "TEXT")
    private String puntosJson;
}
