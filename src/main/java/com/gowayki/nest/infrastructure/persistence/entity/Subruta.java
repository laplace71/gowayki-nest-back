package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subruta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "linea_codigo", nullable = false, length = 20)
    private String lineaCodigo;

    @Column(name = "linea_nombre", nullable = false, length = 100)
    private String lineaNombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linea_id")
    private Linea linea;

    @Column(nullable = false, length = 50, unique = true)
    private String codigo;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(length = 150)
    private String operador;

    @Column(name = "color_hex", length = 9)
    private String colorHex;

    @Column(name = "route_id_ida", length = 30, unique = true)
    private String routeIdIda;

    @Column(name = "route_id_retorno", length = 30)
    private String routeIdRetorno;

    @Column(name = "nombre_retorno", length = 255)
    private String nombreRetorno;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
