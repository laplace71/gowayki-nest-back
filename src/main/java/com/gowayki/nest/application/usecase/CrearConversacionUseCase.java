package com.gowayki.nest.application.usecase;

import com.gowayki.nest.application.dto.ConversacionResponseDto;
import com.gowayki.nest.application.dto.CrearConversacionRequestDto;
import com.gowayki.nest.domain.exception.AccesoDenegadoException;
import com.gowayki.nest.domain.exception.ConflictoException;
import com.gowayki.nest.domain.model.ConversacionModel;
import com.gowayki.nest.domain.model.MensajeModel;
import com.gowayki.nest.domain.repository.ArchivoPendienteBorradoPort;
import com.gowayki.nest.domain.repository.ConversacionRepositoryPort;
import com.gowayki.nest.domain.repository.EliminacionGrupoPort;
import com.gowayki.nest.domain.repository.MensajeRepositoryPort;
import com.gowayki.nest.domain.repository.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class CrearConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ArchivoPendienteBorradoPort archivoPendienteBorradoPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;

    public CrearConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                   UsuarioRepositoryPort usuarioRepositoryPort,
                                   MensajeRepositoryPort mensajeRepositoryPort,
                                   ArchivoPendienteBorradoPort archivoPendienteBorradoPort,
                                   EliminacionGrupoPort eliminacionGrupoPort) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.archivoPendienteBorradoPort = archivoPendienteBorradoPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
    }

    @Transactional
    public ConversacionResponseDto ejecutar(CrearConversacionRequestDto request) {
        if (request.getCreatedBy() != null && usuarioRepositoryPort.findById(request.getCreatedBy()).isEmpty()) {
            throw new IllegalArgumentException("El usuario creador no existe.");
        }

        String titulo = request.getTitulo() != null ? request.getTitulo().trim() : null;
        boolean esGrupo = Boolean.TRUE.equals(request.getEsGrupo());

        // Seguridad: solo el tipo estructurado decide si es un grupo de ruta.
        // Ni el título ni la sola presencia de claveRuta bastan: cualquier
        // usuario podría crear un grupo con un título parecido y, si lo
        // aceptáramos, al entrar a su ruta se lo sacaría de ese grupo.
        String claveRuta = request.getClaveRuta() != null ? request.getClaveRuta().trim() : null;
        boolean tipoRutaExplicito = "GRUPO_RUTA".equals(request.getTipoConversacion());
        boolean esGrupoRuta = esGrupo && tipoRutaExplicito;

        if (esGrupoRuta && (claveRuta == null || claveRuta.isBlank())) {
            claveRuta = claveDesdeTitulo(titulo);
        }
        if (esGrupoRuta && (claveRuta == null || claveRuta.isBlank())) {
            throw new IllegalArgumentException(
                    "Para un grupo de ruta hace falta la subruta (claveRuta).");
        }

        String tipoConversacion = esGrupoRuta ? "GRUPO_RUTA" : (esGrupo ? "GRUPO" : "PRIVADA");

        if (esGrupoRuta) {
            java.util.Optional<ConversacionModel> existenteOpt =
                    conversacionRepositoryPort.findByClaveRuta(claveRuta)
                            .or(() -> titulo == null
                                    ? java.util.Optional.empty()
                                    : conversacionRepositoryPort.findByTitulo(titulo));
            if (existenteOpt.isPresent()) {
                ConversacionModel existente = existenteOpt.get();
                if (eliminacionGrupoPort.buscarPendiente(existente.getId()).isPresent()) {
                    throw new ConflictoException(
                            "Este grupo está pendiente de eliminación definitiva; ya no admite nuevos participantes.");
                }
                Set<UUID> entrando = usuariosEntrando(request);

                // Unirse uno mismo al grupo sigue siendo libre (es lo que hace
                // la app al entrar a una subruta). Invitar a OTROS conductores
                // es una acción de administración: creador o cualquier ADMIN,
                // incluidos los que heredaron el rol.
                UUID solicitante = request.getCreatedBy();
                boolean soloYo = entrando.size() == 1 && entrando.contains(solicitante);
                boolean esCreador = existente.getCreatedBy() != null
                        && existente.getCreatedBy().equals(solicitante);
                boolean esAdmin = conversacionRepositoryPort.esAdministrador(existente.getId(), solicitante);
                if (!soloYo && !esCreador && !esAdmin) {
                    throw new AccesoDenegadoException("Solo un administrador puede añadir miembros.");
                }

                for (UUID pId : entrando) {
                    conversacionRepositoryPort.agregarParticipante(existente.getId(), pId);
                }
                for (UUID pId : entrando) {
                    salirDeOtrosGruposRuta(pId, existente.getId());
                }
                int count = conversacionRepositoryPort.contarParticipantes(existente.getId());
                return ConversacionResponseDto.builder()
                        .id(existente.getId())
                        .titulo(existente.getTitulo())
                        .esGrupo(existente.getEsGrupo())
                        .tipoConversacion(existente.getTipoConversacion())
                        .claveRuta(existente.getClaveRuta())
                        .createdBy(existente.getCreatedBy())
                        .createdAt(existente.getCreatedAt())
                        .totalMensajes(0)
                        .ultimoMensaje(null)
                        .totalParticipantes(count)
                        .build();
            }
        }

        List<UUID> participantes = new ArrayList<>(usuariosEntrando(request));

        ConversacionModel model = ConversacionModel.builder()
                .titulo(titulo)
                .esGrupo(esGrupo)
                .tipoConversacion(tipoConversacion)
                .claveRuta(esGrupoRuta ? claveRuta : null)
                .createdBy(request.getCreatedBy())
                .createdAt(OffsetDateTime.now())
                .build();

        ConversacionModel guardado = conversacionRepositoryPort.save(model, participantes);

        if (esGrupoRuta) {
            for (UUID pId : participantes) {
                salirDeOtrosGruposRuta(pId, guardado.getId());
            }
        }

        int count = conversacionRepositoryPort.contarParticipantes(guardado.getId());

        return ConversacionResponseDto.builder()
                .id(guardado.getId())
                .titulo(guardado.getTitulo())
                .esGrupo(guardado.getEsGrupo())
                .tipoConversacion(guardado.getTipoConversacion())
                .claveRuta(guardado.getClaveRuta())
                .createdBy(guardado.getCreatedBy())
                .createdAt(guardado.getCreatedAt())
                .totalMensajes(0)
                .ultimoMensaje(null)
                .totalParticipantes(count)
                .build();
    }

    /** "GRUPO · Subruta X" -> "Subruta X". Solo para peticiones antiguas ya marcadas como ruta. */
    private static String claveDesdeTitulo(String titulo) {
        if (titulo == null) return null;
        String prefijo = "GRUPO · ";
        return titulo.startsWith(prefijo) ? titulo.substring(prefijo.length()).trim() : null;
    }

    /**
     * ¿Es un grupo de subruta? Solo el tipo estructurado. El título es texto
     * libre y no puede decidir la membresía de un chofer.
     */
    private static boolean esGrupoDeRuta(ConversacionModel conversacion) {
        return "GRUPO_RUTA".equals(conversacion.getTipoConversacion());
    }

    private Set<UUID> usuariosEntrando(CrearConversacionRequestDto request) {
        Set<UUID> ids = new LinkedHashSet<>();
        if (request.getParticipanteIds() != null) {
            ids.addAll(request.getParticipanteIds());
        }
        if (request.getCreatedBy() != null) {
            ids.add(request.getCreatedBy());
        }
        return ids;
    }

    /**
     * Al entrar a un grupo de ruta (subruta), el usuario deja de pertenecer a los
     * demás grupos de ruta: así cada subruta solo tiene a quienes están en ella.
     * Si algún grupo queda vacío, se elimina junto con sus mensajes y audios.
     */
    private void salirDeOtrosGruposRuta(UUID usuarioId, UUID grupoActualId) {
        if (usuarioId == null) return;
        for (ConversacionModel c : conversacionRepositoryPort.findByUsuarioIdSinFiltroOcultas(usuarioId)) {
            if (c.getId() == null || c.getId().equals(grupoActualId)) continue;
            if (!esGrupoDeRuta(c)) continue;

            try {
                conversacionRepositoryPort.quitarParticipante(c.getId(), usuarioId);
            } catch (IllegalArgumentException ignored) {
                // Ya no era participante; nada que hacer.
            }

            if (conversacionRepositoryPort.contarParticipantes(c.getId()) == 0) {
                List<String> audios = mensajeRepositoryPort.findByConversacionId(c.getId()).stream()
                        .map(MensajeModel::getAudioUrl)
                        .filter(Objects::nonNull)
                        .toList();
                conversacionRepositoryPort.eliminar(c.getId());
                // Los archivos se borran después del commit, no aquí.
                audios.forEach(ruta -> archivoPendienteBorradoPort.encolar(ruta, "GRUPO_RUTA_VACIO"));
            }
        }
    }
}

