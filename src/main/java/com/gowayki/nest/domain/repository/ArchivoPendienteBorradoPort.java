package com.gowayki.nest.domain.repository;

import com.gowayki.nest.domain.model.ArchivoPendienteBorradoModel;

import java.util.List;
import java.util.UUID;

/**
 * Cola persistente de archivos a borrar. Se escribe en la misma transacción que
 * elimina los datos; el borrado físico ocurre después, en otro proceso.
 */
public interface ArchivoPendienteBorradoPort {

    /** Encola una ruta si no estaba ya en la cola. */
    void encolar(String ruta, String motivo);

    /** Rutas pendientes, las más antiguas primero. */
    List<ArchivoPendienteBorradoModel> pendientes(int limite);

    /** La ruta se borró (o ya no existía): sale de la cola. */
    void quitar(UUID id);

    /** No se pudo borrar: se cuenta el intento y se reintentará más tarde. */
    void registrarFallo(UUID id, String error);
}
