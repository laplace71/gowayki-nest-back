package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.CrearUnidadRequestDto;
import com.gowayki.nest.domain.model.UnidadModel;
import com.gowayki.nest.domain.repository.UnidadRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearUnidadUseCase {

    private final UnidadRepositoryPort unidadRepositoryPort;

    public CrearUnidadUseCase(UnidadRepositoryPort unidadRepositoryPort) {
        this.unidadRepositoryPort = unidadRepositoryPort;
    }

    @Transactional
    public UnidadModel ejecutar(CrearUnidadRequestDto request) {
        if (request == null) throw new IllegalArgumentException("Faltan datos de la combi.");
        String placa = request.getPlaca() == null ? "" : request.getPlaca().trim();
        String padron = request.getNumeroPadron() == null ? "" : request.getNumeroPadron().trim();
        if (placa.length() < 4) throw new IllegalArgumentException("La placa debe tener al menos 4 caracteres.");
        if (padron.isBlank()) throw new IllegalArgumentException("Indica el número de padrón.");
        if (request.getLineaId() == null) throw new IllegalArgumentException("La línea es obligatoria.");

        boolean repetida = unidadRepositoryPort.findAll().stream()
                .anyMatch(u -> placa.equalsIgnoreCase(u.getPlaca()));
        if (repetida) throw new IllegalArgumentException("Ya existe una combi con esa placa.");

        return unidadRepositoryPort.save(UnidadModel.builder()
                .numeroPadron(padron)
                .placa(placa)
                .lineaId(request.getLineaId())
                .subrutaId(request.getSubrutaId())
                .estado("OPERATIVA")
                .build());
    }
}
