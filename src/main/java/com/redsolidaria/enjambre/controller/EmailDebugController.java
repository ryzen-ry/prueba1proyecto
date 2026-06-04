package com.redsolidaria.enjambre.controller;

import com.redsolidaria.enjambre.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debug/email")
@RequiredArgsConstructor
@Slf4j
public class EmailDebugController {

    private final EmailService emailService;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private String mailPort;

    /**
     * Endpoint para verificar la configuración de email
     */
    @GetMapping("/config")
    public ResponseEntity<?> verificarConfiguracion() {
        log.info("🔍 Verificando configuración de email");

        String response = String.format(
            "📧 CONFIGURACION DE EMAIL:\n" +
            "================================\n" +
            "MAIL_HOST: %s\n" +
            "MAIL_PORT: %s\n" +
            "MAIL_USERNAME: %s\n" +
            "MAIL_PASSWORD: %s\n" +
            "================================\n",
            mailHost != null ? mailHost : "NO CONFIGURADO",
            mailPort != null ? mailPort : "NO CONFIGURADO",
            mailUsername != null ? mailUsername : "NO CONFIGURADO",
            mailUsername != null && !mailUsername.isEmpty() ? "✓ CONFIGURADA" : "❌ NO CONFIGURADA"
        );

        log.info(response);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para enviar un email de prueba de verificación
     */
    @PostMapping("/test-verificacion")
    public ResponseEntity<?> enviarEmailPrueba(@RequestParam String email, @RequestParam String codigo) {
        log.info("📧 Enviando email de prueba a: {} con código: {}", email, codigo);

        try {
            emailService.enviarCodigoVerificacion(email, codigo);
            Thread.sleep(2000); // Espera un poco para que se ejecute el @Async
            return ResponseEntity.ok("✓ Email de verificación enviado a: " + email + "\nRevisa los logs en Railway");
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint para enviar un email de activación de prueba
     */
    @PostMapping("/test-activacion")
    public ResponseEntity<?> enviarActivacionPrueba(@RequestParam String email) {
        log.info("📧 Enviando email de activación de prueba a: {}", email);

        try {
            emailService.enviarCorreoActivacion(email);
            Thread.sleep(2000);
            return ResponseEntity.ok("✓ Email de activación enviado a: " + email);
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
}
