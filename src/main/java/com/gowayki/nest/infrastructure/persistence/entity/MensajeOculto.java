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

/** Un mensaje que este usuario ocultó para sí (los demás lo siguen viendo). */
@Entity
@Table(name = "mensaje_oculto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeOculto {

    @EmbeddedId
    private MensajeOcultoId id;

    @Column(name = "oculto_en", nullable = false)
    @Builder.Default
    private OffsetDateTime ocultoEn = OffsetDateTime.now();
}
