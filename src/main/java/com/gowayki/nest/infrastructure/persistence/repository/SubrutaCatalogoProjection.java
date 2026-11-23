package com.gowayki.nest.infrastructure.persistence.repository;

/**
 * Proyección del catálogo: evita cargar el BLOB de imagen en cada consulta.
 */
public interface SubrutaCatalogoProjection {
    Integer getId();
    String getLineaCodigo();
    String getLineaNombre();
    String getCodigo();
    String getNombre();
    String getOperador();
    String getColorHex();
    String getRouteIdIda();
    String getRouteIdRetorno();
    String getNombreRetorno();
    Boolean getActiva();
}
