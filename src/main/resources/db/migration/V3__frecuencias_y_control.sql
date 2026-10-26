-- 1. Tabla: punto_control (Paraderos y geocercas ordenadas)
CREATE TABLE punto_control (
    id SERIAL PRIMARY KEY,
    linea_id INT NOT NULL REFERENCES linea(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,         -- Ej: 'Pte. Grau / Av. Ejército'
    orden INT NOT NULL,                   -- 1, 2, 3...
    ubicacion GEOMETRY(Point, 4326) NOT NULL,
    radio_tolerancia_metros INT NOT NULL DEFAULT 50,
    tolerancia_minutos INT NOT NULL DEFAULT 2,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_punto_control_linea ON punto_control(linea_id, orden);
CREATE INDEX idx_punto_control_gis ON punto_control USING GIST(ubicacion);

-- 2. Tabla: registro_cumplimiento (Historial de marcaciones por parada)
CREATE TABLE registro_cumplimiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asignacion_id UUID NOT NULL REFERENCES asignacion_turno(id) ON DELETE CASCADE,
    punto_control_id INT NOT NULL REFERENCES punto_control(id) ON DELETE RESTRICT,
    hora_programada TIMESTAMPTZ NOT NULL,
    hora_llegada TIMESTAMPTZ NOT NULL,
    diferencia_minutos INT NOT NULL,
    estado VARCHAR(15) NOT NULL CHECK (estado IN ('A_TIEMPO', 'ATRASADO', 'ADELANTADO')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cumplimiento_asignacion ON registro_cumplimiento(asignacion_id);
