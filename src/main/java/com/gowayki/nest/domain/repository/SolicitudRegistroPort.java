package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.SolicitudRegistroModel;

public interface SolicitudRegistroPort {
    SolicitudRegistroModel guardar(SolicitudRegistroModel solicitud);
}
