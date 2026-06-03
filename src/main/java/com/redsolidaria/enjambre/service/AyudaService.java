package com.redsolidaria.enjambre.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redsolidaria.enjambre.model.*;
import com.redsolidaria.enjambre.repository.*;
import com.redsolidaria.enjambre.ws.AyudaConnectionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AyudaService {

    private static final long MAX_UBICACION_AGE_MS = 120_000; // 2 minutos

    private final UbicacionUsuarioRepository ubicacionUsuarioRepository;
    private final PersonaDiscapacitadaRepository personaDiscapacitadaRepository;
    private final VoluntarioRepository voluntarioRepository;
    private final SolicitudAyudaRepository solicitudAyudaRepository;
    private final SolicitudAyudaIntentoRepository solicitudAyudaIntentoRepository;
    private final UsuarioService usuarioService;
    private final AyudaConnectionRegistry connectionRegistry;

    private final ObjectMapper objectMapper;

    public AyudaService(
            UbicacionUsuarioRepository ubicacionUsuarioRepository,
            PersonaDiscapacitadaRepository personaDiscapacitadaRepository,
            VoluntarioRepository voluntarioRepository,
            SolicitudAyudaRepository solicitudAyudaRepository,
            SolicitudAyudaIntentoRepository solicitudAyudaIntentoRepository,
            UsuarioService usuarioService,
            AyudaConnectionRegistry connectionRegistry,
            ObjectMapper objectMapper
    ) {
        this.ubicacionUsuarioRepository = ubicacionUsuarioRepository;
        this.personaDiscapacitadaRepository = personaDiscapacitadaRepository;
        this.voluntarioRepository = voluntarioRepository;
        this.solicitudAyudaRepository = solicitudAyudaRepository;
        this.solicitudAyudaIntentoRepository = solicitudAyudaIntentoRepository;
        this.usuarioService = usuarioService;
        this.connectionRegistry = connectionRegistry;
        this.objectMapper = objectMapper;
    }

    public void actualizarUbicacion(Long usuarioId, double lat, double lng, Double precisionMetros) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        if (usuario == null) return;

        // Solo guardamos ubicaciones de los roles que participan.
        if (!"DISCAPACITADO".equals(usuario.getRol()) && !"VOLUNTARIO".equals(usuario.getRol())) {
            return;
        }

        UbicacionUsuario ubicacion = ubicacionUsuarioRepository.findByUsuario_Id(usuarioId).orElse(null);
        if (ubicacion == null) {
            ubicacion = new UbicacionUsuario();
            ubicacion.setUsuario(usuario);
        }

        ubicacion.setLatitud(lat);
        ubicacion.setLongitud(lng);
        ubicacion.setPrecisionMetros(precisionMetros);
        ubicacion.setActualizadoEn(LocalDateTime.now());
        ubicacionUsuarioRepository.save(ubicacion);
    }

    public SolicitudAyuda solicitarAyuda(Long discapacitadoId) {
        PersonaDiscapacitada discapacitado = personaDiscapacitadaRepository.findById(discapacitadoId)
                .orElseThrow(() -> new IllegalArgumentException("Discapacitado no encontrado"));

        UbicacionUsuario ubicacionDis = ubicacionUsuarioRepository.findByUsuario_Id(discapacitadoId)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no disponible para el discapacitado"));

        if (ubicacionDis.getActualizadoEn() == null ||
                ubicacionDis.getActualizadoEn().isBefore(LocalDateTime.now().minusNanos(MAX_UBICACION_AGE_MS * 1_000_000))) {
            throw new IllegalArgumentException("Tu ubicación no está actualizada. Activa tu GPS e inténtalo nuevamente.");
        }

        SolicitudAyuda solicitud = new SolicitudAyuda();
        solicitud.setDiscapacitado(discapacitado);
        solicitud.setVoluntarioAceptado(null);
        solicitud.setEstado("PENDIENTE");
        solicitud.setCreadaEn(LocalDateTime.now());

        SolicitudAyuda guardada = solicitudAyudaRepository.save(solicitud);

        // Enviar la primera alerta al voluntario más cercano.
        enviarSiguienteVoluntario(guardada.getId(), ubicacionDis);

        return guardada;
    }

    public void responderAyuda(Long solicitudId, Long voluntarioId, String decision) {
        SolicitudAyuda solicitud = solicitudAyudaRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            return;
        }

        SolicitudAyudaIntento intento = solicitudAyudaIntentoRepository
                .findBySolicitud_IdAndVoluntario_Id(solicitudId, voluntarioId)
                .orElse(null);

        if (intento == null) return;
        if (!"PENDIENTE".equals(intento.getEstado())) return;

        LocalDateTime ahora = LocalDateTime.now();

        if ("ACEPTAR".equalsIgnoreCase(decision)) {
            intento.setEstado("ACEPTADA");
            intento.setRespondidaEn(ahora);
            solicitudAyudaIntentoRepository.save(intento);

            Voluntario voluntario = intento.getVoluntario();
            solicitud.setVoluntarioAceptado(voluntario);
            solicitud.setEstado("ACEPTADA");
            solicitud.setAceptadaEn(ahora);
            solicitudAyudaRepository.save(solicitud);

            // Enviar ubicación e información entre ambos usuarios.
            UbicacionUsuario ubicacionDis = ubicacionUsuarioRepository.findByUsuario_Id(solicitud.getDiscapacitado().getId()).orElse(null);
            UbicacionUsuario ubicacionVol = ubicacionUsuarioRepository.findByUsuario_Id(voluntario.getId()).orElse(null);

            Map<String, Object> payloadDis = new HashMap<>();
            payloadDis.put("type", "SOLICITUD_ACEPTADA");
            payloadDis.put("solicitudId", solicitud.getId());
            payloadDis.put("voluntario", mapVoluntario(voluntario));
            if (ubicacionVol != null) {
                payloadDis.put("ubicacionVoluntario", mapUbicacion(ubicacionVol));
            }

            connectionRegistry.sendToUser(solicitud.getDiscapacitado().getId(), payloadDis);

            Map<String, Object> payloadVol = new HashMap<>();
            payloadVol.put("type", "CONFIRMACION_ACEPTACION");
            payloadVol.put("solicitudId", solicitud.getId());
            payloadVol.put("discapacitado", mapDiscapacitado(solicitud.getDiscapacitado()));
            if (ubicacionDis != null) {
                payloadVol.put("ubicacionDiscapacitado", mapUbicacion(ubicacionDis));
            }

            connectionRegistry.sendToUser(voluntario.getId(), payloadVol);

            return;
        }

        if ("RECHAZAR".equalsIgnoreCase(decision)) {
            intento.setEstado("RECHAZADA");
            intento.setRespondidaEn(ahora);
            solicitudAyudaIntentoRepository.save(intento);

            // Avisar al voluntario que rechazó.
            Map<String, Object> payloadRechazoVol = new HashMap<>();
            payloadRechazoVol.put("type", "SOLICITUD_RECHAZADA");
            payloadRechazoVol.put("solicitudId", solicitudId);
            connectionRegistry.sendToUser(voluntarioId, payloadRechazoVol);

            // También avisar al discapacitado para que vea que se está buscando a otro.
            Map<String, Object> payloadRechazoDis = new HashMap<>();
            payloadRechazoDis.put("type", "SOLICITUD_RECHAZADA");
            payloadRechazoDis.put("solicitudId", solicitudId);
            payloadRechazoDis.put("mensaje", "El voluntario rechazó la solicitud. Buscando otro cercano...");
            connectionRegistry.sendToUser(solicitud.getDiscapacitado().getId(), payloadRechazoDis);

            // Enviar al siguiente más cercano.
            enviarSiguienteVoluntario(solicitudId, null);
        }
    }

    public void cancelarSolicitud(Long solicitudId, Long discapacitadoId) {
        if (solicitudId == null || discapacitadoId == null) return;

        SolicitudAyuda solicitud = solicitudAyudaRepository.findById(solicitudId).orElse(null);
        if (solicitud == null) return;
        if (solicitud.getDiscapacitado() == null || !discapacitadoId.equals(solicitud.getDiscapacitado().getId())) {
            return;
        }
        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            return;
        }

        solicitud.setEstado("CANCELADA");
        solicitudAyudaRepository.save(solicitud);

        // Cancelar intentos pendientes y notificar a los voluntarios para que retiren la tarjeta.
        List<SolicitudAyudaIntento> intentos = solicitudAyudaIntentoRepository.findBySolicitud_Id(solicitudId);
        for (SolicitudAyudaIntento i : intentos) {
            if (i == null) continue;
            if ("PENDIENTE".equals(i.getEstado())) {
                i.setEstado("CANCELADA");
                i.setRespondidaEn(LocalDateTime.now());
                solicitudAyudaIntentoRepository.save(i);
            }

            if (i.getVoluntario() != null) {
                Map<String, Object> payloadVol = new HashMap<>();
                // Reutilizamos un type que el frontend del voluntario ya maneja para remover la tarjeta.
                payloadVol.put("type", "SOLICITUD_RECHAZADA");
                payloadVol.put("solicitudId", solicitudId);
                payloadVol.put("mensaje", "La solicitud fue cancelada por el usuario.");
                connectionRegistry.sendToUser(i.getVoluntario().getId(), payloadVol);
            }
        }

        Map<String, Object> payloadDis = new HashMap<>();
        payloadDis.put("type", "SOLICITUD_CANCELADA");
        payloadDis.put("solicitudId", solicitudId);
        payloadDis.put("mensaje", "Solicitud cancelada.");
        connectionRegistry.sendToUser(discapacitadoId, payloadDis);
    }

    private void enviarSiguienteVoluntario(Long solicitudId, UbicacionUsuario ubicacionDisCache) {
        SolicitudAyuda solicitud = solicitudAyudaRepository.findById(solicitudId).orElse(null);
        if (solicitud == null) return;
        if (!"PENDIENTE".equals(solicitud.getEstado())) return;

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime cutoff = ahora.minusNanos(MAX_UBICACION_AGE_MS * 1_000_000);

        UbicacionUsuario ubicacionDis = ubicacionDisCache != null
                ? ubicacionDisCache
                : ubicacionUsuarioRepository.findByUsuario_Id(solicitud.getDiscapacitado().getId()).orElse(null);

        if (ubicacionDis == null) {
            // Si no tenemos ubicación del discapacitado, cancelamos.
            solicitud.setEstado("CANCELADA");
            solicitudAyudaRepository.save(solicitud);
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "SOLICITUD_CANCELADA");
            payload.put("solicitudId", solicitudId);
            payload.put("mensaje", "No se encontró tu ubicación. Intenta nuevamente.");
            connectionRegistry.sendToUser(solicitud.getDiscapacitado().getId(), payload);
            return;
        }

        List<UbicacionUsuario> voluntariosActivos = ubicacionUsuarioRepository
                .findByUsuario_RolAndActualizadoEnAfter("VOLUNTARIO", cutoff);

        // Evitar enviarle la misma solicitud a voluntarios que ya fueron intentados.
        List<SolicitudAyudaIntento> intentos = solicitudAyudaIntentoRepository.findBySolicitud_Id(solicitudId);
        Set<Long> voluntariosIntentados = new HashSet<>();
        for (SolicitudAyudaIntento i : intentos) {
            if (i.getVoluntario() != null) voluntariosIntentados.add(i.getVoluntario().getId());
        }

        UbicacionUsuario mejor = null;
        double mejorDistanciaKm = Double.MAX_VALUE;

        for (UbicacionUsuario ubicVol : voluntariosActivos) {
            if (ubicVol.getUsuario() == null) continue;
            if (voluntariosIntentados.contains(ubicVol.getUsuario().getId())) continue;
            if (!connectionRegistry.isUserConnected(ubicVol.getUsuario().getId())) continue;

            double distKm = calcularDistanciaKm(
                    ubicacionDis.getLatitud(), ubicacionDis.getLongitud(),
                    ubicVol.getLatitud(), ubicVol.getLongitud()
            );

            if (distKm < mejorDistanciaKm) {
                mejorDistanciaKm = distKm;
                mejor = ubicVol;
            }
        }

        if (mejor == null) {
            solicitud.setEstado("CANCELADA");
            solicitudAyudaRepository.save(solicitud);

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "SOLICITUD_CANCELADA");
            payload.put("solicitudId", solicitudId);
            payload.put("mensaje", "No hay voluntarios conectados y disponibles cerca en este momento.");
            connectionRegistry.sendToUser(solicitud.getDiscapacitado().getId(), payload);
            return;
        }

        Voluntario voluntarioCandidato = (Voluntario) mejor.getUsuario();

        SolicitudAyudaIntento intento = new SolicitudAyudaIntento();
        intento.setSolicitud(solicitud);
        intento.setVoluntario(voluntarioCandidato);
        intento.setEstado("PENDIENTE");
        intento.setCreadaEn(LocalDateTime.now());
        intento.setRespondidaEn(null);
        solicitudAyudaIntentoRepository.save(intento);

        Map<String, Object> payloadVol = new HashMap<>();
        payloadVol.put("type", "NUEVA_SOLICITUD");
        payloadVol.put("solicitudId", solicitudId);
        payloadVol.put("discapacitado", mapDiscapacitado(solicitud.getDiscapacitado()));
        payloadVol.put("ubicacionDiscapacitado", mapUbicacion(ubicacionDis));

        connectionRegistry.sendToUser(voluntarioCandidato.getId(), payloadVol);
    }

    private Map<String, Object> mapUbicacion(UbicacionUsuario u) {
        Map<String, Object> m = new HashMap<>();
        m.put("lat", u.getLatitud());
        m.put("lng", u.getLongitud());
        if (u.getPrecisionMetros() != null) m.put("precisionMetros", u.getPrecisionMetros());
        return m;
    }

    private Map<String, Object> mapVoluntario(Voluntario v) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", v.getId());
        m.put("nombres", v.getNombres());
        m.put("apellidos", v.getApellidos());
        m.put("email", v.getEmail());
        return m;
    }

    private Map<String, Object> mapDiscapacitado(PersonaDiscapacitada d) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", d.getId());
        m.put("nombres", d.getNombres());
        m.put("apellidos", d.getApellidos());
        m.put("telefono", d.getTelefono());
        m.put("direccion", d.getDireccion());
        return m;
    }

    public List<SolicitudAyuda> obtenerHistorialDiscapacitado(Long id) {
        return solicitudAyudaRepository.findByDiscapacitado_IdOrderByCreadaEnDesc(id);
    }

    public List<SolicitudAyuda> obtenerHistorialVoluntario(Long id) {
        return solicitudAyudaRepository.findByVoluntarioAceptado_IdOrderByCreadaEnDesc(id);
    }

    public List<SolicitudAyuda> obtenerTodasLasSolicitudes() {
        return solicitudAyudaRepository.findAll();
    }

    public long obtenerTotalAyudadosPorVoluntario(Long voluntarioId) {
        return solicitudAyudaRepository.countByVoluntarioAceptado_IdAndEstado(voluntarioId, "ACEPTADA");
    }

    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        // Fórmula Haversine para distancias en la Tierra.
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}


