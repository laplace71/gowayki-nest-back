-- Momento en que el usuario se unió al grupo. Sirve para elegir de forma
-- determinista quién hereda la administración cuando el último administrador
-- se va: el participante más antiguo (y, a igualdad, el de menor UUID).
--
-- Reversa: ALTER TABLE conversacion_participante DROP COLUMN agregado_en;
ALTER TABLE conversacion_participante ADD COLUMN agregado_en TIMESTAMPTZ;

-- Las filas existentes no tienen fecha real: se marcan ahora para que la
-- herencia funcione desde el primer día, sin inventar un orden histórico.
UPDATE conversacion_participante SET agregado_en = NOW() WHERE agregado_en IS NULL;

ALTER TABLE conversacion_participante ALTER COLUMN agregado_en SET DEFAULT NOW();
ALTER TABLE conversacion_participante ALTER COLUMN agregado_en SET NOT NULL;

CREATE INDEX idx_participante_antiguedad
    ON conversacion_participante(conversacion_id, agregado_en);
