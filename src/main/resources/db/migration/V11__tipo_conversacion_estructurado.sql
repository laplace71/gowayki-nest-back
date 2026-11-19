-- Tipo de conversación explícito, en lugar de deducirlo del texto del título.
--
-- Hasta ahora el backend adivinaba "esto es un grupo de subruta" mirando si el
-- título empezaba por "GRUPO · ". Eso se rompe en cuanto alguien renombra, y
-- ata la lógica a un texto que es de la interfaz.
--
-- Valores: PRIVADA (chat 1 a 1), GRUPO (grupo normal) y GRUPO_RUTA (grupo de
-- subruta, uno por subruta concreta).
--
-- Reversa: DROP INDEX uq_conversacion_ruta; ALTER TABLE conversacion DROP
-- COLUMN clave_ruta, DROP COLUMN tipo_conversacion;
ALTER TABLE conversacion ADD COLUMN tipo_conversacion VARCHAR(20);
ALTER TABLE conversacion ADD COLUMN clave_ruta VARCHAR(150);

UPDATE conversacion
SET tipo_conversacion = CASE
        WHEN es_grupo AND titulo LIKE 'GRUPO · %' THEN 'GRUPO_RUTA'
        WHEN es_grupo THEN 'GRUPO'
        ELSE 'PRIVADA'
    END;

-- `substring(titulo from 9)` quita exactamente "GRUPO · " (8 caracteres) y
-- conserva el nombre de la subruta como clave estable.
UPDATE conversacion
SET clave_ruta = substring(titulo from 9)
WHERE tipo_conversacion = 'GRUPO_RUTA' AND titulo IS NOT NULL;

ALTER TABLE conversacion ALTER COLUMN tipo_conversacion SET DEFAULT 'PRIVADA';
ALTER TABLE conversacion ALTER COLUMN tipo_conversacion SET NOT NULL;
ALTER TABLE conversacion
    ADD CONSTRAINT ck_conversacion_tipo
    CHECK (tipo_conversacion IN ('PRIVADA', 'GRUPO', 'GRUPO_RUTA'));

-- Una sola conversación por subruta: la clave es la subruta, no el título.
CREATE UNIQUE INDEX uq_conversacion_ruta
    ON conversacion(clave_ruta)
    WHERE tipo_conversacion = 'GRUPO_RUTA';
