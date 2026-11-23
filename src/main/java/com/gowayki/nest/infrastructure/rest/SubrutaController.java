package com.gowayki.nest.infrastructure.rest;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.gowayki.nest.application.dto.CatalogoRutasDto;
import com.gowayki.nest.application.dto.ParaderoRutaDto;
import com.gowayki.nest.application.dto.SubrutaDto;
import com.gowayki.nest.infrastructure.persistence.entity.PuntoControl;
import com.gowayki.nest.infrastructure.persistence.entity.Subruta;
import com.gowayki.nest.infrastructure.persistence.entity.SubrutaGeometria;
import com.gowayki.nest.infrastructure.persistence.repository.SpringDataPuntoControlRepository;
import com.gowayki.nest.infrastructure.persistence.repository.SpringDataSubrutaGeometriaRepository;
import com.gowayki.nest.infrastructure.persistence.repository.SpringDataSubrutaRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rutas")
@CrossOrigin(origins = "*")
public class SubrutaController {

    private final SpringDataSubrutaRepository subrutaRepository;
    private final SpringDataPuntoControlRepository puntoControlRepository;
    private final SpringDataSubrutaGeometriaRepository geometriaRepository;
    private final ObjectMapper objectMapper;

    public SubrutaController(SpringDataSubrutaRepository subrutaRepository,
                             SpringDataPuntoControlRepository puntoControlRepository,
                             SpringDataSubrutaGeometriaRepository geometriaRepository,
                             ObjectMapper objectMapper) {
        this.subrutaRepository = subrutaRepository;
        this.puntoControlRepository = puntoControlRepository;
        this.geometriaRepository = geometriaRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/catalogo")
    public ResponseEntity<CatalogoRutasDto> catalogo() {
        // Proyección sin el BLOB de imagen: el catálogo no usa las fotos.
        List<com.gowayki.nest.infrastructure.persistence.repository.SubrutaCatalogoProjection> subrutas =
                subrutaRepository.findCatalogo();
        Map<String, List<SubrutaDto>> porLinea = new LinkedHashMap<>();
        Map<String, String> nombreLinea = new LinkedHashMap<>();
        Map<String, String> colorLinea = new LinkedHashMap<>();

        for (com.gowayki.nest.infrastructure.persistence.repository.SubrutaCatalogoProjection s : subrutas) {
            String cod = s.getLineaCodigo() == null ? "" : s.getLineaCodigo();
            porLinea.computeIfAbsent(cod, k -> new ArrayList<>());
            nombreLinea.putIfAbsent(cod, s.getLineaNombre() != null ? s.getLineaNombre() : ("Línea " + cod));
            // color de la primera subruta como aproximación de la línea
            colorLinea.putIfAbsent(cod, s.getColorHex());
            porLinea.get(cod).add(toDto(s));
        }

        List<CatalogoRutasDto.LineaCatalogoDto> lineas = new ArrayList<>();
        for (var e : porLinea.entrySet()) {
            lineas.add(CatalogoRutasDto.LineaCatalogoDto.builder()
                    .codigo(e.getKey())
                    .nombre(nombreLinea.getOrDefault(e.getKey(), e.getKey()))
                    .colorHex(colorLinea.get(e.getKey()))
                    .subrutas(e.getValue())
                    .build());
        }

        return ResponseEntity.ok(CatalogoRutasDto.builder().lineas(lineas).build());
    }

    @GetMapping("/subrutas")
    public ResponseEntity<List<SubrutaDto>> listarSubrutas(@RequestParam(required = false) String linea) {
        List<Subruta> list = (linea != null && !linea.isBlank())
                ? subrutaRepository.findByLineaCodigoOrderByNombreAsc(linea)
                : subrutaRepository.findByActivaTrueOrderByLineaCodigoAscNombreAsc();
        return ResponseEntity.ok(list.stream().map(this::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{routeId}/paraderos")
    public ResponseEntity<List<ParaderoRutaDto>> paraderos(@PathVariable String routeId) {
        return resolve(routeId)
                .map(res -> ResponseEntity.ok(puntoControlRepository
                        .findBySubrutaIdAndSentidoOrderByOrdenAsc(res.subruta.getId(), res.sentido)
                        .stream().map(this::toParaderoDto).collect(Collectors.toList())))
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }

    @GetMapping("/{routeId}/geometria")
    public ResponseEntity<List<List<Double>>> geometria(@PathVariable String routeId) {
        Optional<Resolved> resOpt = resolve(routeId);
        if (resOpt.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Resolved res = resOpt.get();
        Optional<SubrutaGeometria> geo = geometriaRepository.findBySubrutaIdAndSentido(res.subruta.getId(), res.sentido);
        if (geo.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        try {
            List<List<Double>> puntos = objectMapper.readValue(geo.get().getPuntosJson(), new TypeReference<List<List<Double>>>() {});
            return ResponseEntity.ok(puntos);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /** Foto de la ruta guardada en la BD; la app la pide por routeId. */
    @GetMapping("/{routeId}/imagen")
    public ResponseEntity<byte[]> imagen(@PathVariable String routeId) {
        byte[] bytes = resolve(routeId)
                .map(r -> r.subruta.getImagen())
                .orElse(null);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/webp"))
                .body(bytes);
    }

    private Optional<Resolved> resolve(String routeId) {
        if (routeId == null || routeId.isBlank()) return Optional.empty();
        Optional<Subruta> ida = subrutaRepository.findByRouteIdIda(routeId);
        if (ida.isPresent()) return Optional.of(new Resolved(ida.get(), "IDA"));
        Optional<Subruta> ret = subrutaRepository.findByRouteIdRetorno(routeId);
        if (ret.isPresent()) return Optional.of(new Resolved(ret.get(), "RETORNO"));
        return Optional.empty();
    }

    private SubrutaDto toDto(com.gowayki.nest.infrastructure.persistence.repository.SubrutaCatalogoProjection s) {
        return SubrutaDto.builder()
                .id(s.getId())
                .lineaCodigo(s.getLineaCodigo())
                .lineaNombre(s.getLineaNombre())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .operador(s.getOperador())
                .colorHex(s.getColorHex())
                .routeIdIda(s.getRouteIdIda())
                .routeIdRetorno(s.getRouteIdRetorno())
                .nombreRetorno(s.getNombreRetorno())
                .activa(s.getActiva())
                .build();
    }

    private SubrutaDto toDto(Subruta s) {
        return SubrutaDto.builder()
                .id(s.getId())
                .lineaCodigo(s.getLineaCodigo())
                .lineaNombre(s.getLineaNombre())
                .codigo(s.getCodigo())
                .nombre(s.getNombre())
                .operador(s.getOperador())
                .colorHex(s.getColorHex())
                .routeIdIda(s.getRouteIdIda())
                .routeIdRetorno(s.getRouteIdRetorno())
                .nombreRetorno(s.getNombreRetorno())
                .activa(s.getActiva())
                .build();
    }

    private ParaderoRutaDto toParaderoDto(PuntoControl p) {
        Double lat = p.getUbicacion() != null ? p.getUbicacion().getY() : null;
        Double lon = p.getUbicacion() != null ? p.getUbicacion().getX() : null;
        return ParaderoRutaDto.builder()
                .id(p.getExternoId() != null ? p.getExternoId() : String.valueOf(p.getId()))
                .name(p.getNombre())
                .lat(lat)
                .lon(lon)
                .district(p.getDistrito())
                .orden(p.getOrden())
                .build();
    }

    private static class Resolved {
        final Subruta subruta;
        final String sentido;
        Resolved(Subruta subruta, String sentido) { this.subruta = subruta; this.sentido = sentido; }
    }
}
