package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mensaje_leido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeLeido {
    @EmbeddedId
    private MensajeLeidoId id;

    @Column(name = "leido_en", nullable = false)
    @Builder.Default
    private OffsetDateTime leidoEn = OffsetDateTime.now();
}
