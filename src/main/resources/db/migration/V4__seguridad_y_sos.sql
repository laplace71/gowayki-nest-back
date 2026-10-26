-- 1. Tabla: alerta_incidencia (Eventos del botón físico SOS y zonas de calor)
CREATE TABLE alerta_incidencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chofer_id UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    unidad_id INT REFERENCES unidad(id) ON DELETE SET NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('EMERGENCIA_SOS', 'ACCIDENTE', 'TRAFICO_DESVIO', 'MECANICA')),
    origen_disparo VARCHAR(20) NOT NULL CHECK (origen_disparo IN ('BOTON_BLE_FISICO', 'APP_PANTALLA')),
    ubicacion GEOMETRY(Point, 4326) NOT NULL,
    descripcion VARCHAR(255),
    audio_url VARCHAR(255),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'ATENDIDA', 'CERRADA')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expira_at TIMESTAMPTZ
);

CREATE INDEX idx_alerta_gis ON alerta_incidencia USING GIST(ubicacion);
CREATE INDEX idx_alerta_activa ON alerta_incidencia(estado, created_at);
