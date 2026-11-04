package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.UnidadPosicionModel;
import com.gowayki.nest.infrastructure.persistence.entity.UnidadPosicion;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
public class UnidadPosicionMapper {

    public UnidadPosicionModel toDomain(UnidadPosicion entity) {
        if (entity == null) {
            return null;
        }

        Double lat = null;
        Double lon = null;

        if (entity.getPosicion() != null) {
            lon = entity.getPosicion().getX();
            lat = entity.getPosicion().getY();
        }

        return UnidadPosicionModel.builder()
                .unidadId(entity.getUnidadId())
                .latitud(lat)
                .longitud(lon)
                .velocidadKmh(entity.getVelocidadKmh() != null ? entity.getVelocidadKmh().doubleValue() : null)
                .rumboGrados(entity.getRumboGrados())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UnidadPosicion toEntity(UnidadPosicionModel model) {
        if (model == null) {
            return null;
        }

        Point point = null;
        if (model.getLatitud() != null && model.getLongitud() != null) {
            point = new GeometryFactory().createPoint(new Coordinate(model.getLongitud(), model.getLatitud()));
            point.setSRID(4326);
        }

        UnidadPosicion entity = new UnidadPosicion();
        entity.setUnidadId(model.getUnidadId());
        entity.setPosicion(point);
        if (model.getVelocidadKmh() != null) {
            entity.setVelocidadKmh(new java.math.BigDecimal(model.getVelocidadKmh().toString()));
        }
        if (model.getRumboGrados() != null) {
            entity.setRumboGrados(model.getRumboGrados());
        }
        if (model.getUpdatedAt() != null) {
            entity.setUpdatedAt(model.getUpdatedAt());
        }
        return entity;
    }
}