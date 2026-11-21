-- V15: elimina subruta_paradero (vista de compatibilidad) y su respaldo legacy.
-- Los paraderos/puntos viven en punto_control (unificados en V13).
DROP VIEW IF EXISTS subruta_paradero;
DROP TABLE IF EXISTS subruta_paradero;
DROP TABLE IF EXISTS subruta_paradero_legacy;
