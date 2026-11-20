-- Lectura por usuario: un mensaje deja de contar como no leído solo para
-- quien lo abrió. La purga física del mensaje elimina estas filas por CASCADE.
CREATE TABLE mensaje_leido (
    mensaje_id UUID NOT NULL REFERENCES mensaje(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    leido_en   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (mensaje_id, usuario_id)
);

CREATE INDEX idx_mensaje_leido_usuario ON mensaje_leido(usuario_id);
