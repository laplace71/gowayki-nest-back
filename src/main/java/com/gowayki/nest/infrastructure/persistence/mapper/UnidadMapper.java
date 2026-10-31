package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.PosicionGpsModel;
import com.gowayki.nest.domain.model.UnidadModel;
import com.gowayki.nest.infrastructure.persistence.entity.Unidad;
import com.gowayki.nest.infrastructure.persistence.entity.UnidadPosicion;
import org.springframework.stereotype.Component;

@Component
public class UnidadMapper {

    public UnidadModel toDomain(Unidad entity, UnidadPosicion posicionEntity) {
        if (entity == null) {
            return null;
        }

        PosicionGpsModel posicionModel = null;
        if (posicionEntity != null && posicionEntity.getPosicion() != null) {
            posicionModel = PosicionGpsModel.builder()
                    .longitud(posicionEntity.getPosicion().getX())
                    .latitud(posicionEntity.getPosicion().getY())
                    .velocidadKmh(posicionEntity.getVelocidadKmh() != null ? posicionEntity.getVelocidadKmh().doubleValue() : 0.0)
                    .rumboGrados(posicionEntity.getRumboGrados())
                    .ultimaActualizacion(posicionEntity.getUpdatedAt() != null ? posicionEntity.getUpdatedAt().toString() : null)
                    .build();
        }

        return UnidadModel.builder()
                .id(entity.getId())
                .numeroPadron(entity.getNumeroPadron())
                .placa(entity.getPlaca())
                .lineaId(entity.getLinea() != null ? entity.getLinea().getId() : null)
                .lineaNombre(entity.getLinea() != null ? entity.getLinea().getNombre() : null)
                .estado(entity.getEstado())
                .posicionActual(posicionModel)
                .build();
    }
}

