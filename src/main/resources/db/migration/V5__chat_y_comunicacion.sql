-- 1. Tabla: conversacion (Salas privadas o grupos de choferes)
CREATE TABLE conversacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(100),                  -- Null si es chat 1 a 1
    es_grupo BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID REFERENCES usuario(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Tabla: conversacion_participante (Miembros y contadores de no leídos)
CREATE TABLE conversacion_participante (
    conversacion_id UUID NOT NULL REFERENCES conversacion(id) ON DELETE CASCADE,
    chofer_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    rol VARCHAR(20) NOT NULL DEFAULT 'MIEMBRO' CHECK (rol IN ('ADMIN', 'MIEMBRO')),
    ultimo_leido_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (conversacion_id, chofer_id)
);

CREATE INDEX idx_part_chofer ON conversacion_participante(chofer_id);

-- 3. Tabla: mensaje (Mensajes de texto y notas de voz con estado)
CREATE TABLE mensaje (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversacion_id UUID NOT NULL REFERENCES conversacion(id) ON DELETE CASCADE,
    emisor_id UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('TEXTO', 'AUDIO')),
    contenido_texto TEXT,
    audio_url VARCHAR(255),
    audio_duracion_segundos INT NOT NULL DEFAULT 0,
    estado VARCHAR(15) NOT NULL DEFAULT 'ENVIADO' CHECK (estado IN ('ENVIADO', 'ENTREGADO', 'LEIDO')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mensaje_conversacion_fecha ON mensaje(conversacion_id, created_at DESC);
