-- V12: estructura de subrutas, paraderos y geometrías.
-- Solo DDL: los datos semilla se eliminaron de las migraciones.
-- Mantiene las tablas que V13/V15 y las entidades JPA esperan.

BEGIN;

-- Subrutas concretas (unidad operativa), agrupadas por línea general
CREATE TABLE IF NOT EXISTS subruta (
    id SERIAL PRIMARY KEY,
    linea_codigo VARCHAR(20) NOT NULL,
    linea_nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    operador VARCHAR(150),
    color_hex VARCHAR(9),
    route_id_ida VARCHAR(30),
    route_id_retorno VARCHAR(30),
    nombre_retorno VARCHAR(255),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_subruta_codigo UNIQUE (codigo),
    CONSTRAINT uq_subruta_route_ida UNIQUE (route_id_ida)
);

CREATE INDEX IF NOT EXISTS idx_subruta_linea_codigo ON subruta(linea_codigo);
CREATE INDEX IF NOT EXISTS idx_subruta_route_ida ON subruta(route_id_ida);
CREATE INDEX IF NOT EXISTS idx_subruta_route_retorno ON subruta(route_id_retorno);

-- Paraderos por subruta y sentido (IDA/RETORNO)
CREATE TABLE IF NOT EXISTS subruta_paradero (
    id BIGSERIAL PRIMARY KEY,
    subruta_id INT NOT NULL REFERENCES subruta(id) ON DELETE CASCADE,
    sentido VARCHAR(10) NOT NULL CHECK (sentido IN ('IDA','RETORNO')),
    externo_id VARCHAR(50),
    nombre VARCHAR(255),
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    orden INT NOT NULL,
    distrito VARCHAR(120),
    CONSTRAINT uq_subruta_paradero UNIQUE (subruta_id, sentido, orden)
);

CREATE INDEX IF NOT EXISTS idx_subruta_paradero_subruta ON subruta_paradero(subruta_id, sentido, orden);

-- Geometría (trazado) por subruta y sentido como JSON array [[lat,lon],...]
CREATE TABLE IF NOT EXISTS subruta_geometria (
    id BIGSERIAL PRIMARY KEY,
    subruta_id INT NOT NULL REFERENCES subruta(id) ON DELETE CASCADE,
    sentido VARCHAR(10) NOT NULL CHECK (sentido IN ('IDA','RETORNO')),
    puntos_json TEXT NOT NULL,
    CONSTRAINT uq_subruta_geometria UNIQUE (subruta_id, sentido)
);

CREATE INDEX IF NOT EXISTS idx_subruta_geometria_subruta ON subruta_geometria(subruta_id, sentido);
