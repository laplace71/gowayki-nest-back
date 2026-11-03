package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.AlertaIncidenciaModel;
import com.gowayki.nest.infrastructure.persistence.entity.AlertaIncidencia;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class AlertaIncidenciaMapper {

    public AlertaIncidenciaModel toDomain(AlertaIncidencia entity) {
        if (entity == null) {
            return null;
        }

        Double lat = null;
        Double lon = null;

        if (entity.getUbicacion() != null) {
            lon = entity.getUbicacion().getX();
            lat = entity.getUbicacion().getY();
        }

        return AlertaIncidenciaModel.builder()
                .id(entity.getId())
                .choferId(entity.getChofer() != null ? entity.getChofer().getId() : null)
                .unidadId(entity.getUnidad() != null ? entity.getUnidad().getId() : null)
                .tipo(entity.getTipo())
                .origenDisparo(entity.getOrigenDisparo())
                .latitud(lat)
                .longitud(lon)
                .descripcion(entity.getDescripcion())
                .audioUrl(entity.getAudioUrl())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .expiraAt(entity.getExpiraAt())
                .build();
    }

    public AlertaIncidencia toEntity(AlertaIncidenciaModel model) {
        if (model == null) {
            return null;
        }

        Point point = null;
        if (model.getLatitud() != null && model.getLongitud() != null) {
            point = new GeometryFactory().createPoint(new Coordinate(model.getLongitud(), model.getLatitud()));
            point.setSRID(4326);
        }

        return AlertaIncidencia.builder()
                .id(model.getId())
                .tipo(model.getTipo())
                .origenDisparo(model.getOrigenDisparo())
                .ubicacion(point)
                .descripcion(model.getDescripcion())
                .audioUrl(model.getAudioUrl())
                .estado(model.getEstado())
                .createdAt(model.getCreatedAt())
                .expiraAt(model.getExpiraAt())
                .build();
    }
}