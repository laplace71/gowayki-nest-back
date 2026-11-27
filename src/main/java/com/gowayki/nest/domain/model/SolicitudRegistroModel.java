package com.gowayki.nest.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Solicitud enviada desde la app (chofer) o el panel (jefe/admin). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRegistroModel {
    private Long id;
    private String tipo;
    private String dni;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String rutaTrabajo;
    private UUID usuarioId;
    private Integer lineaSolicitadaId;
    private Integer subrutaSolicitadaId;
    private String pinHash;
    private String estado;
    private String usuarioAsignado;
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;
}
