package com.redsolidaria.enjambre.controller;

import com.redsolidaria.enjambre.model.DonacionMonetaria;
import com.redsolidaria.enjambre.model.DonacionProducto;
import com.redsolidaria.enjambre.model.Usuario;
import com.redsolidaria.enjambre.service.DonacionService;
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
@RequestMapping("/api/donaciones")
public class DonacionRestController {

    @Autowired
    private DonacionService donacionService;

    @GetMapping("/historial")
    public ResponseEntity<?> obtenerHistorialDonaciones(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Debes iniciar sesión"));
        }

        try {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
            
            List<DonacionMonetaria> monetarias = donacionService.obtenerTodasMonetarias();
            List<DonacionProducto> productos = donacionService.obtenerTodasProductos();
            
            if (!isAdmin) {
                String userEmail = usuario.getEmail();
                monetarias = monetarias.stream()
                        .filter(d -> userEmail.equalsIgnoreCase(d.getEmail()))
                        .collect(Collectors.toList());
                productos = productos.stream()
                        .filter(d -> userEmail.equalsIgnoreCase(d.getEmail()))
                        .collect(Collectors.toList());
            }

            List<Map<String, Object>> resMonetarias = monetarias.stream().map(m -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.getId());
                map.put("monto", m.getMonto());
                map.put("celular", m.getCelular());
                map.put("codigoYape", m.getCodigoYape());
                map.put("estado", m.getEstado());
                map.put("fecha", m.getFechaDonacion());
                return map;
            }).collect(Collectors.toList());

            List<Map<String, Object>> resProductos = productos.stream().map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("tipoProducto", p.getTipoProducto());
                map.put("estadoProducto", p.getEstadoProducto());
                map.put("opcionEntrega", p.getOpcionEntrega());
                map.put("direccion", p.getDireccion());
                map.put("horario", p.getHorario());
                map.put("comentarios", p.getComentarios());
                map.put("estado", p.getEstado());
                map.put("fecha", p.getFechaDonacion());
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("monetarias", resMonetarias);
            result.put("productos", resProductos);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
