package com.gowayki.nest.infrastructure.persistence.mapper;

import com.gowayki.nest.domain.model.AsignacionTurnoModel;
import com.gowayki.nest.infrastructure.persistence.entity.AsignacionTurno;
import com.gowayki.nest.infrastructure.persistence.entity.Unidad;
import com.gowayki.nest.infrastructure.persistence.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class AsignacionTurnoMapper {

    public AsignacionTurnoModel toDomain(AsignacionTurno entity) {
        if (entity == null) {
            return null;
        }

        return AsignacionTurnoModel.builder()
                .id(entity.getId())
                .choferId(entity.getChofer() != null ? entity.getChofer().getId() : null)
                .choferNombre(entity.getChofer() != null ? entity.getChofer().getNombre() : null)
                .unidadId(entity.getUnidad() != null ? entity.getUnidad().getId() : null)
                .numeroPadron(entity.getUnidad() != null ? entity.getUnidad().getNumeroPadron() : null)
                .placa(entity.getUnidad() != null ? entity.getUnidad().getPlaca() : null)
                .lineaId(entity.getUnidad() != null && entity.getUnidad().getLinea() != null ? entity.getUnidad().getLinea().getId() : null)
                .lineaNombre(entity.getUnidad() != null && entity.getUnidad().getLinea() != null ? entity.getUnidad().getLinea().getNombre() : null)
                .lineaColorHex(entity.getUnidad() != null && entity.getUnidad().getLinea() != null ? entity.getUnidad().getLinea().getColorHex() : null)
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .estado(entity.getEstado())
                .porcentajeCumplimiento(entity.getPorcentajeCumplimiento())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AsignacionTurno toEntity(AsignacionTurnoModel model, Usuario chofer, Unidad unidad) {
        if (model == null) {
            return null;
        }

        return AsignacionTurno.builder()
                .id(model.getId())
                .chofer(chofer)
                .unidad(unidad)
                .fecha(model.getFecha())
                .horaInicio(model.getHoraInicio())
                .horaFin(model.getHoraFin())
                .estado(model.getEstado() != null ? model.getEstado() : "EN_RUTA")
                .porcentajeCumplimiento(model.getPorcentajeCumplimiento())
                .createdAt(model.getCreatedAt())
                .build();
    }
}
