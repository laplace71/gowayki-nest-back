-- V23: altas de registro creadas desde la app (chofer y jefe de ruta).
-- Solo DDL: la tabla puede existir ya si la creó el backend Django sobre la
-- misma base; en ese caso esta migración no cambia nada.

CREATE TABLE IF NOT EXISTS solicitud_registro (
    id                    BIGSERIAL PRIMARY KEY,
    tipo                  VARCHAR(20)  NOT NULL DEFAULT 'CHOFER',
    dni                   VARCHAR(8)   NOT NULL,
    nombre_completo       VARCHAR(100) NOT NULL,
    telefono              VARCHAR(9),
    email                 VARCHAR(120),
    placa_numero          VARCHAR(10),
    ruta_trabajo          VARCHAR(100),
    usuario_id            UUID,
    linea_solicitada_id   INTEGER,
    subruta_solicitada_id INTEGER,
    pin_hash              VARCHAR(255),
    estado                VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    usuario_asignado      VARCHAR(50),
    creado_en             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    actualizado_en        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_solicitud_registro_estado ON solicitud_registro(estado);
CREATE INDEX IF NOT EXISTS idx_solicitud_registro_dni ON solicitud_registro(dni);
