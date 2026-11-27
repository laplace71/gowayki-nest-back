package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.SolicitudRegistroModel;
import com.gowayki.nest.domain.repository.SolicitudRegistroPort;
import com.gowayki.nest.infrastructure.persistence.entity.SolicitudRegistro;
import org.springframework.stereotype.Component;

@Component
public class SolicitudRegistroPersistenceAdapter implements SolicitudRegistroPort {

    private final SpringDataSolicitudRegistroRepository repository;

    public SolicitudRegistroPersistenceAdapter(SpringDataSolicitudRegistroRepository repository) {
        this.repository = repository;
    }

    @Override
    public SolicitudRegistroModel guardar(SolicitudRegistroModel model) {
        SolicitudRegistro entity = SolicitudRegistro.builder()
                .tipo(model.getTipo())
                .dni(model.getDni())
                .nombreCompleto(model.getNombreCompleto())
                .telefono(model.getTelefono())
                .email(model.getEmail())
                .rutaTrabajo(model.getRutaTrabajo())
                .usuarioId(model.getUsuarioId())
                .lineaSolicitadaId(model.getLineaSolicitadaId())
                .subrutaSolicitadaId(model.getSubrutaSolicitadaId())
                .pinHash(model.getPinHash())
                .estado(model.getEstado())
                .usuarioAsignado(model.getUsuarioAsignado())
                .creadoEn(model.getCreadoEn())
                .actualizadoEn(model.getActualizadoEn())
                .build();
        SolicitudRegistro guardada = repository.save(entity);
        model.setId(guardada.getId());
        return model;
    }
}
