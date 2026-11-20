-- Borrado diferido de archivos (audios). El archivo NO se borra dentro de la
-- transacción que elimina la conversación: se apunta aquí y un proceso aparte
-- lo elimina cuando el commit ya está firme.
--
-- Así un rollback no deja archivos huérfanos eliminados y un fallo del disco se
-- puede reintentar sin volver a tocar la base.
CREATE TABLE archivo_pendiente_borrado (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ruta              VARCHAR(500) NOT NULL,
    motivo            VARCHAR(40),
    creado_en         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    intentos          INTEGER NOT NULL DEFAULT 0,
    ultimo_intento_en TIMESTAMPTZ,
    ultimo_error      VARCHAR(500)
);

-- El limpiador toma los más antiguos primero.
CREATE INDEX idx_archivo_pendiente_creado ON archivo_pendiente_borrado(creado_en);

-- La misma ruta no debe encolarse dos veces mientras esté pendiente.
CREATE UNIQUE INDEX uq_archivo_pendiente_ruta ON archivo_pendiente_borrado(ruta);
