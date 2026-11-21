-- "Eliminar para todos": el mensaje se oculta de inmediato (soft delete) y
-- se programa su purga física a las 72 horas. No hay cancelación: el usuario
-- no ve ni se le informa el plazo; el job lo elimina solo.
--
-- Tabla propia para no mezclarla con la eliminación de grupos: el soft delete
-- de esos se puede cancelar y desmarcar, aquí no.

CREATE TABLE mensaje_eliminacion (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mensaje_id       UUID NOT NULL REFERENCES mensaje(id) ON DELETE CASCADE,
    conversacion_id  UUID NOT NULL REFERENCES conversacion(id) ON DELETE CASCADE,
    solicitado_por   UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    solicitado_en    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ejecutar_despues TIMESTAMPTZ NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE', 'EJECUTADA')),
    ejecutada_en     TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_mensaje_eliminacion_pendiente
    ON mensaje_eliminacion(mensaje_id) WHERE estado = 'PENDIENTE';

CREATE INDEX idx_mensaje_eliminacion_vencidas
    ON mensaje_eliminacion(ejecutar_despues) WHERE estado = 'PENDIENTE';
