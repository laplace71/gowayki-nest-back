-- V14: evita turnos activos/programados duplicados por chofer o por unidad (carreras/doble envío)
-- Limpieza defensiva: conserva el turno más antiguo en caso de existir duplicados
WITH ranked AS (
  SELECT id, row_number() OVER (PARTITION BY chofer_id ORDER BY created_at ASC, hora_inicio ASC, id ASC) rn
  FROM asignacion_turno WHERE estado IN ('EN_RUTA','PROGRAMADO')
)
UPDATE asignacion_turno t SET estado='FINALIZADO', hora_fin = COALESCE(t.hora_fin, NOW())
FROM ranked r WHERE t.id=r.id AND r.rn > 1;

WITH ranked AS (
  SELECT id, row_number() OVER (PARTITION BY unidad_id ORDER BY created_at ASC, hora_inicio ASC, id ASC) rn
  FROM asignacion_turno WHERE estado IN ('EN_RUTA','PROGRAMADO')
)
UPDATE asignacion_turno t SET estado='FINALIZADO', hora_fin = COALESCE(t.hora_fin, NOW())
FROM ranked r WHERE t.id=r.id AND r.rn > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_turno_activo_chofer ON asignacion_turno(chofer_id) WHERE estado IN ('EN_RUTA','PROGRAMADO');
CREATE UNIQUE INDEX IF NOT EXISTS uq_turno_activo_unidad ON asignacion_turno(unidad_id) WHERE estado IN ('EN_RUTA','PROGRAMADO');
