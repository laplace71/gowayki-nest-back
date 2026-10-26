-- 1. Extensiones del sistema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Tabla: linea (Rutas y corredores)
CREATE TABLE linea (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,          -- Ej: 'Línea 7', 'Línea 11'
    origen VARCHAR(100) NOT NULL,         -- Ej: 'Pte. Grau'
    destino VARCHAR(100) NOT NULL,        -- Ej: 'Paucarpata'
    corredor VARCHAR(50),
    color_hex VARCHAR(7) NOT NULL DEFAULT '#E53E3E',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. Tabla: unidad (Combis y buses)
CREATE TABLE unidad (
    id SERIAL PRIMARY KEY,
    numero_padron VARCHAR(10) NOT NULL,   -- Ej: '08', '03' (como en Figma)
    placa VARCHAR(10) UNIQUE NOT NULL,
    linea_id INT NOT NULL REFERENCES linea(id) ON DELETE RESTRICT,
    estado VARCHAR(20) NOT NULL DEFAULT 'OPERATIVA' CHECK (estado IN ('OPERATIVA', 'MANTENIMIENTO', 'BAJA')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_unidad_linea ON unidad(linea_id);

-- 4. Tabla: unidad_posicion (Telemetría viva para combi adelante/atrás)
CREATE TABLE unidad_posicion (
    unidad_id INT PRIMARY KEY REFERENCES unidad(id) ON DELETE CASCADE,
    posicion GEOMETRY(Point, 4326) NOT NULL,
    velocidad_kmh DECIMAL(5,2) NOT NULL DEFAULT 0.0,
    rumbo_grados INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índice espacial para que los cálculos de distancia en PostGIS vuelen
CREATE INDEX idx_unidad_posicion_gis ON unidad_posicion USING GIST(posicion);
