-- Eliminación definitiva de un grupo: se registra la solicitud y se ejecuta
-- 72 horas después, salvo que se cancele antes.
--
-- `conversacion_id` es nullable a propósito: cuando la purga borra la
-- conversación, la solicitud sobrevive como registro de lo ocurrido
-- (`ON DELETE SET NULL` + `titulo_snapshot`).
CREATE TABLE conversacion_eliminacion (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversacion_id   UUID REFERENCES conversacion(id) ON DELETE SET NULL,
    titulo_snapshot   VARCHAR(100),
    solicitado_por    UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    solicitado_en     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ejecutar_despues  TIMESTAMPTZ NOT NULL,
    estado            VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                      CHECK (estado IN ('PENDIENTE', 'CANCELADA', 'EJECUTADA')),
    cancelada_por     UUID REFERENCES usuario(id) ON DELETE SET NULL,
    cancelada_en      TIMESTAMPTZ,
    ejecutada_en      TIMESTAMPTZ
);

-- Solo puede haber una solicitud viva por conversación.
CREATE UNIQUE INDEX uq_eliminacion_pendiente
    ON conversacion_eliminacion(conversacion_id)
    WHERE estado = 'PENDIENTE';

-- La purga busca "pendientes ya vencidas"; es la consulta más frecuente.
CREATE INDEX idx_eliminacion_vencidas
    ON conversacion_eliminacion(ejecutar_despues)
    WHERE estado = 'PENDIENTE';
