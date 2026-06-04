package com.redsolidaria.enjambre.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Async
    public void enviarCodigoVerificacion(String emailDestino, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("Codigo de verificacion - Red Solidaria UTP");
        mensaje.setText("Hola,\n\nTu codigo de verificacion es: " + codigo +
                        "\n\nEste codigo expira en 10 minutos.\n\n" +
                        "Si no solicitaste este codigo, ignora este mensaje.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "verificación", emailDestino, codigo);
    }

    @Async
    public void enviarCorreoActivacion(String emailDestino) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("Tu cuenta ha sido activada - Red Solidaria UTP");
        mensaje.setText("Hola,\n\nTu cuenta ha sido activada con exito. Ya puedes iniciar sesion en la plataforma.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "activación", emailDestino);
    }

    @Async
    public void enviarCorreoRechazo(String emailDestino) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("❌ Tu cuenta no fue activada - Red Solidaria UTP");
        mensaje.setText("Hola,\n\nTu cuenta no fue activada porque no cumple los requisitos. Puedes volver a registrarte corrigiendo la información.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "rechazo", emailDestino);
    }

    @Async
    public void enviarConfirmacionMonetaria(String emailDestino, String nombre, Double monto) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("💖 ¡Tu donación monetaria ha sido confirmada! - Red Solidaria UTP");
        mensaje.setText("Hola " + nombre + ",\n\n" +
                        "Queremos agradecerte de todo corazón por tu generosa donación monetaria de S/. " + String.format("%.2f", monto) + ".\n" +
                        "Tu contribución ha sido verificada y confirmada con éxito. Gracias a ti, podremos seguir brindando apoyo y adquiriendo productos de primera necesidad para quienes más lo necesitan.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "confirmación monetaria", emailDestino, "monto: S/. " + monto);
    }

    @Async
    public void enviarRechazoMonetaria(String emailDestino, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("⚠️ Actualización sobre tu donación monetaria - Red Solidaria UTP");
        mensaje.setText("Hola " + nombre + ",\n\n" +
                        "Lamentamos informarte que no hemos podido verificar el código de tu donación monetaria.\n" +
                        "Por este motivo, la donación ha sido marcada como rechazada. Si crees que se trata de un error, por favor ponte en contacto con nosotros o intenta registrarla nuevamente.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "rechazo monetaria", emailDestino);
    }

    @Async
    public void enviarConfirmacionProductoRecoger(String emailDestino, String nombre, String producto, String horario) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("📦 ¡Tu donación de producto ha sido aprobada! (Recojo en domicilio) - Red Solidaria UTP");
        mensaje.setText("Hola " + nombre + ",\n\n" +
                        "Nos alegra informarte que tu donación de producto (" + producto + ") ha sido aprobada.\n" +
                        "Hemos coordinado la entrega bajo la opción de: Recoger en domicilio.\n" +
                        "Un miembro de nuestro equipo se acercará a la dirección proporcionada dentro del horario seleccionado:\n" +
                        "⏰ Horario de recojo: " + horario + "\n\n" +
                        "Por favor, ten el producto listo. ¡Muchas gracias por tu valioso apoyo!\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "confirmación de recojo", emailDestino, "producto: " + producto);
    }

    @Async
    public void enviarConfirmacionProductoLlevar(String emailDestino, String nombre, String producto, String direccionSede, String horarioAtencion) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("📦 ¡Tu donación de producto ha sido aprobada! (Llevar a sede) - Red Solidaria UTP");
        mensaje.setText("Hola " + nombre + ",\n\n" +
                        "Nos alegra informarte que tu donación de producto (" + producto + ") ha sido aprobada.\n" +
                        "Puedes acercarte a nuestra sede para realizar la entrega:\n" +
                        "📍 Dirección de la sede: " + direccionSede + "\n" +
                        "⏰ Horario de atención: " + horarioAtencion + "\n\n" +
                        "¡Muchas gracias por tu valioso apoyo para nuestra comunidad!\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "confirmación de entrega en sede", emailDestino, "producto: " + producto);
    }

    @Async
    public void enviarRechazoProducto(String emailDestino, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailFrom);
        mensaje.setTo(emailDestino);
        mensaje.setSubject("⚠️ Actualización sobre tu donación de producto - Red Solidaria UTP");
        mensaje.setText("Hola " + nombre + ",\n\n" +
                        "Agradecemos enormemente tu intención de donar.\n" +
                        "Lamentablemente, en esta ocasión no podemos recibir el producto propuesto debido a políticas internas o falta de capacidad de almacenamiento para este tipo de implemento.\n" +
                        "Esperamos poder contar con tu ayuda en futuras oportunidades.\n\n" +
                        "Saludos,\nEquipo Red Solidaria UTP");

        enviarMensaje(mensaje, "rechazo de producto", emailDestino);
    }

    /**
     * Método privado para enviar un mensaje con logging consistente
     */
    private void enviarMensaje(SimpleMailMessage mensaje, String tipoMensaje, String emailDestino, String... detalles) {
        try {
            // Validar configuración de email
            if (emailFrom == null || emailFrom.isEmpty()) {
                log.error("❌ ERROR: spring.mail.username no está configurado en Railway");
                log.error("   Asegúrate de haber agregado la variable MAIL_USERNAME en el dashboard de Railway");
                return;
            }

            log.info("📧 Intentando enviar correo de {} a: {}", tipoMensaje, emailDestino);
            if (detalles.length > 0) {
                log.debug("   Detalles: {}", String.join(", ", detalles));
            }

            mailSender.send(mensaje);
            
            log.info("✅ Correo de {} enviado exitosamente a: {}", tipoMensaje, emailDestino);
            if (detalles.length > 0) {
                log.info("   {} | {}", tipoMensaje, String.join(" | ", detalles));
            }

        } catch (MailException e) {
            String errorMsg = e.getMessage();
            log.error("❌ ERROR al enviar correo de {} a {}", tipoMensaje, emailDestino);
            log.error("   Tipo de error: {}", e.getClass().getSimpleName());
            log.error("   Mensaje: {}", errorMsg);

            // Dar pistas específicas según el tipo de error
            if (errorMsg != null && errorMsg.toLowerCase().contains("authentication")) {
                log.error("   💡 SOLUCIÓN: Revisa tu contraseña de aplicación Gmail en MAIL_PASSWORD");
                log.error("      1. Ve a myaccount.google.com/apppasswords");
                log.error("      2. Genera una contraseña nueva");
                log.error("      3. Actualiza MAIL_PASSWORD en Railway sin espacios");
            } else if (errorMsg != null && errorMsg.toLowerCase().contains("connect")) {
                log.error("   💡 SOLUCIÓN: Verifica la conectividad a smtp.gmail.com:587");
            }

            log.debug("Stack trace:", e);

        } catch (Exception e) {
            log.error("❌ ERROR inesperado al enviar correo de {} a {}", tipoMensaje, emailDestino);
            log.error("   Tipo: {}", e.getClass().getName());
            log.error("   Mensaje: {}", e.getMessage());
            log.debug("Stack trace:", e);
        }
    }

    /**
     * Método para validar que la configuración de correo esté correcta
     */
    public void validarConfiguracion() {
        log.info("🔍 Validando configuración de correo...");
        
        if (emailFrom == null || emailFrom.isEmpty()) {
            log.warn("⚠️ MAIL_USERNAME no configurado");
        } else {
            log.info("✅ MAIL_USERNAME: {}", emailFrom);
        }
        
        log.info("✅ MAIL_HOST: {}", mailHost);
        log.info("✅ MAIL_PORT: {}", mailPort);
        log.info("✅ Configuración lista para enviar correos");
    }
}
