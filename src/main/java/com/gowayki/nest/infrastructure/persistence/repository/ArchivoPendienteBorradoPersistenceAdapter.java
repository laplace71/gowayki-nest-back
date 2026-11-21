package com.gowayki.nest.infrastructure.persistence.repository;

import com.gowayki.nest.domain.model.ArchivoPendienteBorradoModel;
import com.gowayki.nest.domain.repository.ArchivoPendienteBorradoPort;
import com.gowayki.nest.infrastructure.persistence.entity.ArchivoPendienteBorrado;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ArchivoPendienteBorradoPersistenceAdapter implements ArchivoPendienteBorradoPort {

    private final SpringDataArchivoPendienteBorradoRepository repository;

    public ArchivoPendienteBorradoPersistenceAdapter(SpringDataArchivoPendienteBorradoRepository repository) {
        this.repository = repository;
    }

    /**
     * Se une a la transacción en curso: la fila de la cola y el borrado de los
     * datos se confirman o se deshacen juntos. Si no hay transacción, abre una.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void encolar(String ruta, String motivo) {
        if (ruta == null || ruta.isBlank() || repository.existsByRuta(ruta)) {
            return;
        }
        repository.save(ArchivoPendienteBorrado.builder()
                .ruta(ruta)
                .motivo(motivo)
                .build());
    }

    @Override
    public List<ArchivoPendienteBorradoModel> pendientes(int limite) {
        // Se reintenta como mucho cada 5 minutos por archivo.
        OffsetDateTime antes = OffsetDateTime.now().minusMinutes(5);
        return repository.findParaIntentar(antes, PageRequest.of(0, Math.max(1, limite)))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void quitar(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void registrarFallo(UUID id, String error) {
        repository.findById(id).ifPresent(entity -> {
            entity.setIntentos(entity.getIntentos() == null ? 1 : entity.getIntentos() + 1);
            entity.setUltimoIntentoEn(OffsetDateTime.now());
            String recortado = error == null ? null : error.substring(0, Math.min(500, error.length()));
            entity.setUltimoError(recortado);
            repository.save(entity);
        });
    }

    private ArchivoPendienteBorradoModel toDomain(ArchivoPendienteBorrado entity) {
        return ArchivoPendienteBorradoModel.builder()
                .id(entity.getId())
                .ruta(entity.getRuta())
                .motivo(entity.getMotivo())
                .creadoEn(entity.getCreadoEn())
                .intentos(entity.getIntentos())
                .ultimoIntentoEn(entity.getUltimoIntentoEn())
                .ultimoError(entity.getUltimoError())
                .build();
    }
}
