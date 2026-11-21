-- Ocultar una conversación es una acción PERSONAL: la fila de `conversacion`,
-- sus participantes y sus mensajes no se tocan. Solo se apunta que ese usuario
-- no quiere verla en su lista.
--
-- Restricción única por conversación y usuario: la clave primaria compuesta ya
-- lo garantiza (no se puede duplicar el par).
CREATE TABLE conversacion_usuario_estado (
    conversacion_id UUID NOT NULL REFERENCES conversacion(id) ON DELETE CASCADE,
    usuario_id      UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    oculta          BOOLEAN NOT NULL DEFAULT FALSE,
    oculta_en       TIMESTAMPTZ,
    PRIMARY KEY (conversacion_id, usuario_id)
);

-- El listado pide "las ocultas de este usuario", así que conviene el índice
-- parcial: son pocas filas frente al total.
CREATE INDEX idx_conversacion_oculta_usuario
    ON conversacion_usuario_estado(usuario_id)
    WHERE oculta;
