-- 1. Tabla: usuario (Choferes, Administradores, Jefes de ruta)
CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    dni VARCHAR(8) UNIQUE NOT NULL,
    telefono VARCHAR(9) UNIQUE NOT NULL,
    email VARCHAR(120) UNIQUE,
    pin_hash VARCHAR(255) NOT NULL,
    foto_url VARCHAR(255),
    rol VARCHAR(20) NOT NULL DEFAULT 'CHOFER' CHECK (rol IN ('CHOFER', 'ADMIN', 'JEFE_RUTA')),
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuario_dni ON usuario(dni);
CREATE INDEX idx_usuario_telefono ON usuario(telefono);

-- 2. Tabla: asignacion_turno (Jornada del conductor en una combi)
CREATE TABLE asignacion_turno (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chofer_id UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    unidad_id INT NOT NULL REFERENCES unidad(id) ON DELETE RESTRICT,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    hora_inicio TIMESTAMPTZ NOT NULL,
    hora_fin TIMESTAMPTZ,
    estado VARCHAR(20) NOT NULL DEFAULT 'EN_RUTA' CHECK (estado IN ('PROGRAMADO', 'EN_RUTA', 'FINALIZADO', 'CANCELADO')),
    porcentaje_cumplimiento DECIMAL(5,2) DEFAULT 0.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_turno_chofer_fecha ON asignacion_turno(chofer_id, fecha);
CREATE INDEX idx_turno_unidad ON asignacion_turno(unidad_id);
