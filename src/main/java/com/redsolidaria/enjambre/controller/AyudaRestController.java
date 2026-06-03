package com.redsolidaria.enjambre.controller;

import com.redsolidaria.enjambre.model.SolicitudAyuda;
import com.redsolidaria.enjambre.model.Usuario;
import com.redsolidaria.enjambre.service.AyudaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ayuda")
public class AyudaRestController {

    @Autowired
    private AyudaService ayudaService;

    @GetMapping("/historial")
    public ResponseEntity<?> obtenerHistorial(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        try {
            List<SolicitudAyuda> solicitudes;
            if ("DISCAPACITADO".equals(usuario.getRol())) {
                solicitudes = ayudaService.obtenerHistorialDiscapacitado(usuario.getId());
            } else if ("VOLUNTARIO".equals(usuario.getRol())) {
                solicitudes = ayudaService.obtenerHistorialVoluntario(usuario.getId());
            } else {
                solicitudes = ayudaService.obtenerTodasLasSolicitudes();
            }

            List<Map<String, Object>> resultado = solicitudes.stream().map(s -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", s.getId());
                item.put("estado", s.getEstado());
                item.put("creadaEn", s.getCreadaEn());
                item.put("aceptadaEn", s.getAceptadaEn());
                
                if (s.getDiscapacitado() != null) {
                    item.put("discapacitado", Map.of(
                        "nombres", s.getDiscapacitado().getNombres(),
                        "apellidos", s.getDiscapacitado().getApellidos(),
                        "telefono", s.getDiscapacitado().getTelefono()
                    ));
                }
                
                if (s.getVoluntarioAceptado() != null) {
                    item.put("voluntario", Map.of(
                        "nombres", s.getVoluntarioAceptado().getNombres(),
                        "apellidos", s.getVoluntarioAceptado().getApellidos(),
                        "email", s.getVoluntarioAceptado().getEmail()
                    ));
                } else {
                    item.put("voluntario", null);
                }
                
                return item;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        try {
            Map<String, Object> stats = new HashMap<>();
            if ("VOLUNTARIO".equals(usuario.getRol())) {
                long totalAyudados = ayudaService.obtenerTotalAyudadosPorVoluntario(usuario.getId());
                stats.put("totalAyudados", totalAyudados);
            } else {
                stats.put("totalAyudados", 0);
            }
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
