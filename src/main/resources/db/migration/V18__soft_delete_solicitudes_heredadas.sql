-- Datos heredados: las solicitudes de eliminación creadas con el código anterior
-- no marcaron `eliminado_en`. La regla 2 exige que esos grupos ya NO se vean en
-- ninguna app (aunque la purga física siga ocurriendo en su fecha original).
UPDATE conversacion c
SET eliminado_en = e.solicitado_en
FROM conversacion_eliminacion e
WHERE e.conversacion_id = c.id
  AND e.estado = 'PENDIENTE'
  AND c.eliminado_en IS NULL;

UPDATE mensaje m
SET eliminado_en = e.solicitado_en
FROM conversacion_eliminacion e
WHERE e.conversacion_id = m.conversacion_id
  AND e.estado = 'PENDIENTE'
  AND m.eliminado_en IS NULL;
