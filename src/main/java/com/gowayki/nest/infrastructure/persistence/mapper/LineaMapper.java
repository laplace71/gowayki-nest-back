package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.LineaModel;
import com.gowayki.nest.domain.model.PuntoControlModel;
import com.gowayki.nest.infrastructure.persistence.entity.Linea;
import com.gowayki.nest.infrastructure.persistence.entity.PuntoControl;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LineaMapper {

    public LineaModel toDomain(Linea entity, List<PuntoControl> puntosEntity) {
        if (entity == null) {
            return null;
        }

        List<PuntoControlModel> puntosModel = (puntosEntity == null) ? Collections.emptyList() :
                puntosEntity.stream().map(this::toPuntoControlDomain).toList();

        return LineaModel.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .origen(entity.getOrigen())
                .destino(entity.getDestino())
                .corredor(entity.getCorredor())
                .colorHex(entity.getColorHex())
                .puntosControl(puntosModel)
                .build();
    }

    public PuntoControlModel toPuntoControlDomain(PuntoControl entity) {
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

