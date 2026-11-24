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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CrearConversacionUseCase {

    private final ConversacionRepositoryPort conversacionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final MensajeRepositoryPort mensajeRepositoryPort;
    private final ArchivoPendienteBorradoPort archivoPendienteBorradoPort;
    private final EliminacionGrupoPort eliminacionGrupoPort;
    private final MensajeSistemaService mensajeSistemaService;

    public CrearConversacionUseCase(ConversacionRepositoryPort conversacionRepositoryPort,
                                   UsuarioRepositoryPort usuarioRepositoryPort,
                                   MensajeRepositoryPort mensajeRepositoryPort,
                                   ArchivoPendienteBorradoPort archivoPendienteBorradoPort,
                                   EliminacionGrupoPort eliminacionGrupoPort,
                                   MensajeSistemaService mensajeSistemaService) {
        this.conversacionRepositoryPort = conversacionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.mensajeRepositoryPort = mensajeRepositoryPort;
        this.archivoPendienteBorradoPort = archivoPendienteBorradoPort;
        this.eliminacionGrupoPort = eliminacionGrupoPort;
        this.mensajeSistemaService = mensajeSistemaService;
    }

    @Transactional
    public ConversacionResponseDto ejecutar(CrearConversacionRequestDto request) {
        if (request.getCreatedBy() != null && usuarioRepositoryPort.findById(request.getCreatedBy()).isEmpty()) {
            throw new IllegalArgumentException("El usuario creador no existe.");
        }

        String titulo = request.getTitulo() != null ? request.getTitulo().trim() : null;
        boolean esGrupo = Boolean.TRUE.equals(request.getEsGrupo());

        // Seguridad: solo el tipo estructurado decide si es un grupo de ruta.
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
        if (esGrupoRuta && !claveRutaValida(claveRuta)) {
            throw new IllegalArgumentException(
                    "claveRuta no válida para un grupo de ruta: " + claveRuta);
        }

        String tipoConversacion = esGrupoRuta ? "GRUPO_RUTA" : (esGrupo ? "GRUPO" : "PRIVADA");

        if (esGrupoRuta) {
            Set<UUID> entrando = usuariosEntrando(request);
            UUID solicitante = request.getCreatedBy();

            // Unirse uno mismo al grupo de su ruta es el flujo normal de la app.
            // Se delega al método público para que el cambio de turno use EXACTAMENTE
            // la misma lógica y no haya dos caminos que se pierdan la limpieza.
            boolean soloYo = entrando.size() == 1 && entrando.contains(solicitante);
            if (soloYo) {
                return asegurarGrupoRuta(solicitante, claveRuta, titulo);
            }

            Optional<ConversacionModel> existenteOpt =
                    conversacionRepositoryPort.findByClaveRuta(claveRuta)
                            .or(() -> titulo == null
                                    ? Optional.empty()
                                    : conversacionRepositoryPort.findByTitulo(titulo));
            if (existenteOpt.isPresent()) {
                ConversacionModel existente = existenteOpt.get();
                if (!Boolean.TRUE.equals(existente.getEsGrupo())
                        || !"GRUPO_RUTA".equals(existente.getTipoConversacion())) {
                    throw new AccesoDenegadoException(
                            "Ese título pertenece a una conversación que no es un grupo de ruta.");
                }
                if (eliminacionGrupoPort.buscarPendiente(existente.getId()).isPresent()) {
                    throw new ConflictoException(
                            "Este grupo está pendiente de eliminación definitiva; ya no admite nuevos participantes.");
                }

                // Invitar a OTROS conductores es administración: creador o cualquier ADMIN.
                boolean esCreador = existente.getCreatedBy() != null
                        && existente.getCreatedBy().equals(solicitante);
                boolean esAdmin = conversacionRepositoryPort.esAdministrador(existente.getId(), solicitante);
                if (!esCreador && !esAdmin) {
                    throw new AccesoDenegadoException("Solo un administrador puede añadir miembros.");
                }

                for (UUID pId : entrando) {
                    boolean yaEraParticipante = conversacionRepositoryPort
                            .existeParticipante(existente.getId(), pId);
                    conversacionRepositoryPort.agregarParticipante(existente.getId(), pId);
                    if (!yaEraParticipante) {
                        mensajeSistemaService.avisar(existente.getId(), pId, "se unió al grupo");
                    }
                }
                for (UUID pId : entrando) {
                    salirDeOtrosGruposRuta(pId, existente.getId());
                }
                return dtoDesde(existeDeConConteo(existente));
            }
        }

        // Crear un grupo de ruta es unirse uno mismo: no se aceptan ids de
        // otros conductores, porque salirDeOtrosGruposRuta los sacaría de sus
        // rutas actuales sin invitación ni permiso.
        Set<UUID> entrando = usuariosEntrando(request);
        List<UUID> participantes = esGrupoRuta && request.getCreatedBy() != null
                ? new ArrayList<>(List.of(request.getCreatedBy()))
                : new ArrayList<>(entrando);

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

        return dtoDesde(existeDeConConteo(guardado));
    }

    /**
     * Asegura que un conductor pertenezca AL grupo de ruta de su subruta y a
     * ningún otro. Es el único punto por el que pasan:
     *  - el join que hace la app al entrar a una subruta, y
     *  - el cambio/subida de turno en el backend.
     *
     * Así la membresía deja de depender de que el cliente recuerde llamar.
     */
    @Transactional
    public ConversacionResponseDto asegurarGrupoRuta(UUID usuarioId, String claveRuta, String titulo) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El id del conductor es obligatorio.");
        }
        String clave = claveRuta != null ? claveRuta.trim() : null;
        if (clave == null || clave.isBlank() || !claveRutaValida(clave)) {
            throw new IllegalArgumentException("claveRuta no válida para un grupo de ruta: " + claveRuta);
        }
        if (usuarioRepositoryPort.findById(usuarioId).isEmpty()) {
            throw new IllegalArgumentException("El conductor no existe.");
        }

        // SOLO la clave identifica al grupo de ruta. Buscar también por título
        // podía reutilizar el grupo de otra subruta cuando el turno cambiaba de
        // código pero traía el nombre viejo: exactamente el bug de aislamiento.
        Optional<ConversacionModel> porTitulo = titulo == null
                ? Optional.empty()
                : conversacionRepositoryPort.findByTitulo(titulo.trim());
        if (porTitulo.isPresent()
                && (!Boolean.TRUE.equals(porTitulo.get().getEsGrupo())
                    || !"GRUPO_RUTA".equals(porTitulo.get().getTipoConversacion()))) {
            throw new AccesoDenegadoException(
                    "Ese título pertenece a una conversación que no es un grupo de ruta.");
        }

        Optional<ConversacionModel> existenteOpt = conversacionRepositoryPort.findByClaveRuta(clave);

        if (existenteOpt.isPresent()) {
            ConversacionModel existente = existenteOpt.get();
            if (!Boolean.TRUE.equals(existente.getEsGrupo())
                    || !"GRUPO_RUTA".equals(existente.getTipoConversacion())) {
                throw new AccesoDenegadoException(
                        "Ese título pertenece a una conversación que no es un grupo de ruta.");
            }
            if (eliminacionGrupoPort.buscarPendiente(existente.getId()).isPresent()) {
                throw new ConflictoException(
                        "Este grupo está pendiente de eliminación definitiva; ya no admite nuevos participantes.");
            }
            boolean yaEraParticipante = conversacionRepositoryPort
                    .existeParticipante(existente.getId(), usuarioId);
            conversacionRepositoryPort.agregarParticipante(existente.getId(), usuarioId);
            if (!yaEraParticipante) {
                mensajeSistemaService.avisar(existente.getId(), usuarioId, "se unió al grupo");
            }
            salirDeOtrosGruposRuta(usuarioId, existente.getId());
            return dtoDesde(existeDeConConteo(existente));
        }

        String tituloFinal = titulo != null && !titulo.isBlank()
                ? titulo.trim()
                : "GRUPO · " + clave;
        ConversacionModel model = ConversacionModel.builder()
                .titulo(tituloFinal)
                .esGrupo(true)
                .tipoConversacion("GRUPO_RUTA")
                .claveRuta(clave)
                .createdBy(usuarioId)
                .createdAt(OffsetDateTime.now())
                .build();
        ConversacionModel guardado = conversacionRepositoryPort.save(model, List.of(usuarioId));
        salirDeOtrosGruposRuta(usuarioId, guardado.getId());
        return dtoDesde(existeDeConConteo(guardado));
    }

    /** Recarga la conversación para que el DTO lleve el conteo de participantes actual. */
    private ConversacionModel existeDeConConteo(ConversacionModel modelo) {
        return conversacionRepositoryPort.findById(modelo.getId()).orElse(modelo);
    }

    private ConversacionResponseDto dtoDesde(ConversacionModel c) {
        int count = conversacionRepositoryPort.contarParticipantes(c.getId());
        return ConversacionResponseDto.builder()
                .id(c.getId())
                .titulo(c.getTitulo())
                .esGrupo(c.getEsGrupo())
                .tipoConversacion(c.getTipoConversacion())
                .claveRuta(c.getClaveRuta())
                .createdBy(c.getCreatedBy())
                .createdAt(c.getCreatedAt())
                .totalMensajes(0)
                .ultimoMensaje(null)
                .totalParticipantes(count)
                .build();
    }

    /**
     * La clave de un grupo de ruta identifica una subruta concreta (SB-…),
     * nunca el tipo de conversación ni un código de línea.
     */
    private static boolean claveRutaValida(String claveRuta) {
        if (claveRuta == null || claveRuta.isBlank()) return false;
        if ("GRUPO_RUTA".equalsIgnoreCase(claveRuta)) return false;
        if (claveRuta.matches("(?i)(l[ií]nea\\s+|canal\\s+de\\s+(l[ií]nea\\s+)?)?c\\d+")) return false;
        if (claveRuta.regionMatches(true, 0, "GRUPO ", 0, 6)) return false;
        return true;
    }

    /** "GRUPO · Subruta X" -> "Subruta X". Solo para peticiones antiguas ya marcadas como ruta. */
    private static String claveDesdeTitulo(String titulo) {
        if (titulo == null) return null;
        String prefijo = "GRUPO · ";
        return titulo.startsWith(prefijo) ? titulo.substring(prefijo.length()).trim() : null;
    }

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
            conversacionRepositoryPort.limpiarEstadoUsuario(c.getId(), usuarioId);
            if (Boolean.TRUE.equals(c.getEsGrupo())
                    || "GRUPO_RUTA".equals(c.getTipoConversacion())) {
                conversacionRepositoryPort.promoverAdministradorSiHaceFalta(c.getId());
            }

            if (conversacionRepositoryPort.contarParticipantes(c.getId()) == 0) {
                List<String> audios = mensajeRepositoryPort.findByConversacionId(c.getId()).stream()
                        .map(MensajeModel::getAudioUrl)
                        .filter(Objects::nonNull)
                        .toList();
                conversacionRepositoryPort.eliminar(c.getId());
                audios.forEach(ruta -> archivoPendienteBorradoPort.encolar(ruta, "GRUPO_RUTA_VACIO"));
            }
        }
    }
}
