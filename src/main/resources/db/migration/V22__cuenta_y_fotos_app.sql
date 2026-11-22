-- Columnas y restricciones introducidas por la app: cuenta desactivable,
-- revocación de tokens, foto de ruta y avisos de sistema.
-- Se escribe de forma idempotente porque en la VPS ya se aplicaron a mano.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'usuario' AND column_name = 'activo') THEN
        ALTER TABLE usuario ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'usuario' AND column_name = 'token_version') THEN
        ALTER TABLE usuario ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'subruta' AND column_name = 'imagen_url') THEN
        ALTER TABLE subruta DROP COLUMN imagen_url;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'subruta' AND column_name = 'imagen') THEN
        ALTER TABLE subruta ADD COLUMN imagen BYTEA;
    END IF;
END $$;

ALTER TABLE mensaje DROP CONSTRAINT IF EXISTS mensaje_tipo_check;
ALTER TABLE mensaje ADD CONSTRAINT mensaje_tipo_check
    CHECK (tipo IN ('TEXTO', 'AUDIO', 'SISTEMA'));
