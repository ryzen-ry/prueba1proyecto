package com.redsolidaria.enjambre.controller;

import com.redsolidaria.enjambre.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para DIAGNOSTICAR problemas de envío de correos en Railway.
 * Endpoints útiles para debug - NO para producción
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class TestEmailController {

    private final EmailService emailService;

    /**
     * Endpoint para verificar la configuración de correo
     * Uso: GET /api/debug/email/config
     */
    @GetMapping("/email/config")
    public ResponseEntity<?> verificarConfiguracion() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            // Validar configuración
            emailService.validarConfiguracion();
            
            config.put("status", "✅ CONFIGURACIÓN DETECTADA");
            config.put("mensaje", "Las variables de ambiente se leyeron correctamente.");
            config.put("instrucciones", "Revisa los logs de la aplicación para más detalles (busca líneas que empiezan con ✅ o ❌)");
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            config.put("status", "❌ ERROR");
            config.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(config);
        }
    }

    /**
     * Endpoint para enviar un correo de prueba
     * Uso: POST /api/debug/email/test
     * Body: {"email": "tuemial@gmail.com"}
     */
    @PostMapping("/email/test")
    public ResponseEntity<?> enviarCorreoDePrueba(@RequestBody Map<String, String> payload) {
        Map<String, Object> respuesta = new HashMap<>();
        
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            respuesta.put("error", "❌ Campo 'email' requerido");
            return ResponseEntity.badRequest().body(respuesta);
        }

        try {
            // Enviar correo de prueba
            emailService.enviarCodigoVerificacion(email, "000000");
            
            respuesta.put("status", "✅ CORREO ENVIADO");
            respuesta.put("email", email);
            respuesta.put("instrucciones", new String[]{
                "1. Revisa los logs en Railway (Dashboard → Logs)",
                "2. Busca líneas que empiezan con '✅' o '❌'",
                "3. Revisa tu bandeja de entrada (y SPAM)",
                "4. Si ves '❌ ERROR', el mensaje te dirá qué está mal"
            });
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("error", "❌ Excepción al enviar: " + e.getMessage());
            return ResponseEntity.badRequest().body(respuesta);
        }
    }

    /**
     * Endpoint para activación de prueba
     * Uso: POST /api/debug/email/activacion
     * Body: {"email": "tuemial@gmail.com"}
     */
    @PostMapping("/email/activacion")
    public ResponseEntity<?> pruebaActivacion(@RequestBody Map<String, String> payload) {
        Map<String, Object> respuesta = new HashMap<>();
        
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            respuesta.put("error", "❌ Campo 'email' requerido");
            return ResponseEntity.badRequest().body(respuesta);
        }

        try {
            emailService.enviarCorreoActivacion(email);
            
            respuesta.put("status", "✅ CORREO DE ACTIVACIÓN ENVIADO");
            respuesta.put("email", email);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("error", "❌ Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(respuesta);
        }
    }
}

