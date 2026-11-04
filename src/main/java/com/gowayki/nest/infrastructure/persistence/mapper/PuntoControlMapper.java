package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.PuntoControlModel;
import com.gowayki.nest.infrastructure.persistence.entity.PuntoControl;
import org.springframework.stereotype.Component;

@Component
public class PuntoControlMapper {

    public PuntoControlModel toDomain(PuntoControl entity) {
        if (entity == null) {
            return null;
        }

        Double lat = null;
        Double lon = null;

        if (entity.getUbicacion() != null) {
            lon = entity.getUbicacion().getX();
            lat = entity.getUbicacion().getY();
        }

        return PuntoControlModel.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .orden(entity.getOrden())
                .latitud(lat)
                .longitud(lon)
                .radioToleranciaMetros(entity.getRadioToleranciaMetros())
                .toleranciaMinutos(entity.getToleranciaMinutos())
                .build();
    }
}