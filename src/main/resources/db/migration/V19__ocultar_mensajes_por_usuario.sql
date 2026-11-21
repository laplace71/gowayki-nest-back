-- "Eliminar para mí": marca personal y persistente de un mensaje oculto.
-- El mensaje sigue en la BD y los demás participantes lo ven normalmente.
-- Al borrarse el mensaje físicamente (purga de 72 h), la fila desaparece
-- automáticamente por la FK ON DELETE CASCADE.

CREATE TABLE mensaje_oculto (
    mensaje_id UUID NOT NULL REFERENCES mensaje(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    oculto_en  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (mensaje_id, usuario_id)
);

CREATE INDEX idx_mensaje_oculto_usuario ON mensaje_oculto(usuario_id);
