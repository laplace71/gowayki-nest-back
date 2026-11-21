-- Regla de negocio 1: "limpiar" un chat es SOLO VISUAL y personal.
-- La marca se guarda en la BD para que sobreviva a reinstalar la app y a
-- cambiar de dispositivo. NO se borra ningún mensaje ni se avisa a los demás.
--
-- Al listar conversaciones se oculta la que tenga limpiado_en y no tenga
-- mensajes posteriores; al listar mensajes se omiten los anteriores a la marca.

ALTER TABLE conversacion_usuario_estado ADD COLUMN limpiado_en TIMESTAMPTZ;

CREATE INDEX idx_conversacion_limpia_usuario
    ON conversacion_usuario_estado(usuario_id)
    WHERE limpiado_en IS NOT NULL;
