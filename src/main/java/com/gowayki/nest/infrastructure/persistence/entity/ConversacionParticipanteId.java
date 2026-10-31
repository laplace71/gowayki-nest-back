package com.gowayki.nest.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ConversacionParticipanteId implements Serializable {

    @Column(name = "conversacion_id")
    private UUID conversacionId;

    @Column(name = "chofer_id")
    private UUID choferId;
}
