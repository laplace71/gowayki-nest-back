package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Solicitud de borrado definitivo de un mensaje para todos, con purga a las 72 h. */
@Entity
@Table(name = "mensaje_eliminacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EliminacionMensaje {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String EJECUTADA = "EJECUTADA";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "mensaje_id", nullable = false)
    private UUID mensajeId;

    @Column(name = "conversacion_id", nullable = false)
    private UUID conversacionId;

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

    @Column(name = "ejecutada_en")
    private OffsetDateTime ejecutadaEn;
}
