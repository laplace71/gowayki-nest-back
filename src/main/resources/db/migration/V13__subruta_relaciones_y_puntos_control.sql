-- V13: cerrar modelo de punta a punta
-- - Formaliza relación subruta <-> linea
-- - Vincula unidad y asignacion_turno a subruta concreta
-- - Unifica paraderos/puntos de control por subruta en punto_control (PostGIS),
--   migrando desde subruta_paradero y manteniendo compatibilidad

BEGIN;

-- 1) Formalizar subruta.linea_id -> linea(id)
ALTER TABLE subruta ADD COLUMN IF NOT EXISTS linea_id INT;
UPDATE subruta s
SET linea_id = l.id
FROM linea l
WHERE s.linea_id IS NULL
  AND (l.nombre = s.linea_nombre OR l.nombre = ('Línea ' || s.linea_codigo) OR l.nombre = s.linea_codigo);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_subruta_linea') THEN
        ALTER TABLE subruta
            ADD CONSTRAINT fk_subruta_linea
            FOREIGN KEY (linea_id) REFERENCES linea(id) ON DELETE RESTRICT;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_subruta_linea_id ON subruta(linea_id);

-- 2) Vincular unidad y asignacion_turno a subruta concreta
ALTER TABLE unidad ADD COLUMN IF NOT EXISTS subruta_id INT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_unidad_subruta') THEN
        ALTER TABLE unidad
            ADD CONSTRAINT fk_unidad_subruta
            FOREIGN KEY (subruta_id) REFERENCES subruta(id) ON DELETE SET NULL;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_unidad_subruta_id ON unidad(subruta_id);

ALTER TABLE asignacion_turno ADD COLUMN IF NOT EXISTS subruta_id INT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_asignacion_turno_subruta') THEN
        ALTER TABLE asignacion_turno
            ADD CONSTRAINT fk_asignacion_turno_subruta
            FOREIGN KEY (subruta_id) REFERENCES subruta(id) ON DELETE SET NULL;
    END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_asignacion_turno_subruta_id ON asignacion_turno(subruta_id);

-- 3) Unificar paraderos/puntos de control por subruta en punto_control (PostGIS)
ALTER TABLE punto_control ADD COLUMN IF NOT EXISTS subruta_id INT;
ALTER TABLE punto_control ADD COLUMN IF NOT EXISTS sentido VARCHAR(10);
ALTER TABLE punto_control ADD COLUMN IF NOT EXISTS externo_id VARCHAR(50);
ALTER TABLE punto_control ADD COLUMN IF NOT EXISTS distrito VARCHAR(120);
ALTER TABLE punto_control ALTER COLUMN linea_id DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_punto_control_subruta') THEN
        ALTER TABLE punto_control
            ADD CONSTRAINT fk_punto_control_subruta
            FOREIGN KEY (subruta_id) REFERENCES subruta(id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_punto_control_sentido') THEN
        ALTER TABLE punto_control
            ADD CONSTRAINT ck_punto_control_sentido
            CHECK (sentido IS NULL OR sentido IN ('IDA','RETORNO'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_punto_control_subruta ON punto_control(subruta_id, sentido, orden);
CREATE INDEX IF NOT EXISTS idx_punto_control_externo ON punto_control(externo_id);

-- Migrar paraderos de subruta a puntos de control unificados (sin duplicar)
INSERT INTO punto_control (linea_id, subruta_id, sentido, externo_id, nombre, orden, ubicacion, radio_tolerancia_metros, tolerancia_minutos, distrito, created_at)
SELECT s.linea_id,
       sp.subruta_id,
       sp.sentido,
       sp.externo_id,
       COALESCE(NULLIF(sp.nombre, ''), 'Paradero ' || sp.orden),
       sp.orden,
       ST_SetSRID(ST_MakePoint(sp.longitud, sp.latitud), 4326),
       50,
       2,
       sp.distrito,
       NOW()
FROM subruta_paradero sp
JOIN subruta s ON s.id = sp.subruta_id
WHERE NOT EXISTS (
    SELECT 1 FROM punto_control pc
    WHERE pc.subruta_id = sp.subruta_id
      AND pc.sentido = sp.sentido
      AND pc.orden = sp.orden
);

-- Vista de compatibilidad: expone subruta_paradero desde punto_control unificado
DROP TABLE IF EXISTS subruta_paradero_legacy;
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'subruta_paradero'
          AND table_type = 'BASE TABLE'
    ) THEN
        ALTER TABLE subruta_paradero RENAME TO subruta_paradero_legacy;
    END IF;
END $$;

CREATE OR REPLACE VIEW subruta_paradero AS
SELECT pc.id,
       pc.subruta_id,
       pc.sentido,
       pc.externo_id,
       pc.nombre,
       ST_Y(pc.ubicacion::geometry) AS latitud,
       ST_X(pc.ubicacion::geometry) AS longitud,
       pc.orden,
       pc.distrito
FROM punto_control pc
WHERE pc.subruta_id IS NOT NULL AND pc.sentido IS NOT NULL;

COMMIT;
