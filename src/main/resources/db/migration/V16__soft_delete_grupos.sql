-- Eliminación total de un grupo (regla de negocio 2):
--   * Al pedirla, la conversación y sus mensajes se MARCAN como eliminados
--     (eliminado_en) para que desaparezcan al instante de todas las apps.
--   * Los renglones siguen en la BD 72 horas. El scheduler los purga con un
--     DELETE físico (ON DELETE CASCADE se lleva participantes y estados).
--   * Si se cancela dentro del plazo, las marcas se limpian y el grupo vuelve.
--
-- La limpieza individual de chat (regla 1) NO necesita BD: es solo visual y
-- vive en el dispositivo.

ALTER TABLE conversacion ADD COLUMN eliminado_en TIMESTAMPTZ;
ALTER TABLE mensaje ADD COLUMN eliminado_en TIMESTAMPTZ;

CREATE INDEX idx_conversacion_viva
    ON conversacion(created_at DESC)
    WHERE eliminado_en IS NULL;

CREATE INDEX idx_mensaje_vivo
    ON mensaje(conversacion_id, created_at)
    WHERE eliminado_en IS NULL;
