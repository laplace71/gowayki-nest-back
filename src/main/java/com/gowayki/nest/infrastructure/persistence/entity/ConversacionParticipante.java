package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "conversacion_participante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionParticipante {

    @EmbeddedId
    private ConversacionParticipanteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversacionId")
    @JoinColumn(name = "conversacion_id")
    private Conversacion conversacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("choferId")
    @JoinColumn(name = "chofer_id")
    private Usuario chofer;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String rol = "MIEMBRO";

    @Column(name = "ultimo_leido_at", nullable = false)
    @Builder.Default
    private OffsetDateTime ultimoLeidoAt = OffsetDateTime.now();
}
