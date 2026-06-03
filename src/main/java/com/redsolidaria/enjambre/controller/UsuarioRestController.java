package com.redsolidaria.enjambre.controller;

import com.redsolidaria.enjambre.model.Usuario;
import com.redsolidaria.enjambre.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No iniciado sesión"));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", usuario.getId());
        data.put("nombres", usuario.getNombres());
        data.put("apellidos", usuario.getApellidos());
        data.put("email", usuario.getEmail());
        data.put("rol", usuario.getRol());
        data.put("verificado", usuario.isVerificado());
        data.put("estado", usuario.getEstado());
        data.put("fechaRegistro", usuario.getFechaRegistro());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/todos")
    public ResponseEntity<?> obtenerTodos(HttpSession session) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRol())) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }
        
        List<Usuario> usuarios = usuarioService.listarTodosUsuarios();
        List<Map<String, Object>> result = usuarios.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("nombreCompleto", u.getNombreCompleto());
            map.put("email", u.getEmail());
            map.put("rol", u.getRol());
            map.put("verificado", u.isVerificado());
            map.put("estado", u.getEstado());
            map.put("fechaRegistro", u.getFechaRegistro());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
}
