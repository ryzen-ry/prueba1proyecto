# 📧 Guía de Configuración de Correos en Railway - Red Solidaria UTP

## Estado Actual
✅ El código Java está correctamente configurado para enviar correos  
✅ La dependencia `spring-boot-starter-mail` está agregada al `pom.xml`  
✅ El `EmailService` tiene métodos listos para enviar diferentes tipos de correos  
⚠️ **PENDIENTE:** Configurar variables de entorno en Railway

---

## 🚀 Paso 1: Generar Contraseña de Aplicación Gmail

**IMPORTANTE:** No uses tu contraseña de Gmail directamente. Google requiere una contraseña de aplicación.

### Instrucciones:

1. **Habilitar autenticación de dos factores:**
   - Ve a [myaccount.google.com](https://myaccount.google.com)
   - Click en "Seguridad" (lado izquierdo)
   - Busca "Verificación en dos pasos" 
   - Si no está activada, actívala (necesitarás un teléfono)

2. **Generar contraseña de aplicación:**
   - Vuelve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
   - Selecciona:
     - **App:** Mail
     - **Device:** Windows PC (o tu dispositivo)
   - Google te mostrará una contraseña de 16 caracteres (con espacios)
   - **COPIA LA CONTRASEÑA SIN LOS ESPACIOS** - Esto es IMPORTANTE

   Ejemplo:
   ```
   Google te muestra: abcd efgh ijkl mnop
   Usa en Railway:     abcdefghijklmnop
   ```

---

## 📋 Paso 2: Acceder al Dashboard de Railway

1. Ve a [railway.app](https://railway.app)
2. Inicia sesión con tu cuenta
3. Selecciona tu proyecto "prueba1proyecto"
4. En el lado izquierdo, busca tu aplicación (debe estar desplegada)
5. Haz click en ella

---

## ⚙️ Paso 3: Configurar Variables de Entorno en Railway

En el panel de tu aplicación:

1. **Ve a la pestaña "Variables"**
2. **Agrega estas 4 variables:**

| Variable | Valor |
|----------|-------|
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | tu_correo@gmail.com |
| `MAIL_PASSWORD` | abcdefghijklmnop (SIN espacios) |

**Detalles importantes:**

- **MAIL_USERNAME:** Usa el correo de Gmail completo (ejemplo: redsolidaria@gmail.com)
- **MAIL_PASSWORD:** La contraseña de 16 caracteres sin espacios (NO es tu contraseña de Gmail)
- **MAIL_HOST:** Debe ser exactamente `smtp.gmail.com`
- **MAIL_PORT:** Debe ser exactamente `587`

---

## 🔄 Paso 4: Redeploy de la Aplicación

Después de configurar las variables:

1. En Railway, ve a la sección de "Deployments"
2. Haz click en "Deploy" o "Redeploy Latest"
3. Espera a que se complete el despliegue (10-15 minutos)

---

## ✅ Paso 5: Verificar que Funciona

### A) Prueba Manual en Navegador:

1. Ve a tu aplicación en Railway
2. Intenta registrar una nueva cuenta
3. Deberías recibir un email de verificación

### B) Revisar Logs:

Si no recibes el correo:

1. En Railway, ve a "Logs"
2. Busca mensajes con ✓ o ❌:
   ```
   ✓ Correo enviado a: usuario@email.com | Código: 123456
   ❌ ERROR al enviar correo de verificación...
   ```

3. Si ves ❌, revisa el mensaje de error
   - Si dice "AuthenticationFailedException" → La contraseña es incorrecta
   - Si dice "SMTPAuthenticationException" → El usuario/contraseña no coinciden
   - Si dice "ConnectionException" → Problema de conectividad (raro en Railway)

---

## 🐛 Solución de Problemas

### Problema: "AuthenticationFailedException"
**Causa:** Contraseña incorrecta  
**Solución:** 
- Regresa a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
- Verifica que copiaste la contraseña SIN espacios
- Intenta generar una contraseña nueva

### Problema: "SMTPAuthenticationException"
**Causa:** El email o contraseña son incorrectos  
**Solución:**
- Verifica que MAIL_USERNAME sea el email Gmail completo (redsolidaria@gmail.com)
- Verifica que MAIL_PASSWORD sea la contraseña de 16 caracteres sin espacios

### Problema: No recibo ningún correo
**Causas posibles:**
1. Las variables no se guardaron correctamente en Railway
2. La aplicación no se redesplegó después de cambiar variables
3. El email de destino está en la bandeja de spam

**Soluciones:**
- Verifica en Railway → Variables que todas las 4 variables están presentes
- Revisa los logs en Railway para ver si hay errores
- Busca el correo en tu carpeta de SPAM

---

## 📧 Tipos de Correos Configurados

El sistema puede enviar automáticamente:

1. **Código de Verificación** - Al registrarse
2. **Confirmación de Activación** - Cuando admin activa cuenta
3. **Rechazo de Solicitud** - Cuando admin rechaza una solicitud
4. **Confirmación de Donación Monetaria** - Cuando se aprueba una donación
5. **Rechazo de Donación Monetaria** - Cuando se rechaza una donación
6. **Confirmación de Producto (Recojo)** - Cuando se aprueba recoger en domicilio
7. **Confirmación de Producto (Sede)** - Cuando se aprueba llevar a sede
8. **Rechazo de Producto** - Cuando se rechaza un producto

---

## 🔐 Seguridad

⚠️ **IMPORTANTE:**
- ❌ NO compartas la contraseña de aplicación Gmail
- ❌ NO hagas commits con la contraseña en el código
- ✅ Usa SIEMPRE variables de entorno en Railway
- ✅ La contraseña está segura en Railway (no se muestra en logs)

---

## 📞 Próximos Pasos

Una vez que los emails funcionen:

1. **Notificaciones en Tiempo Real:** El WebSocket ya está configurado (sin emails)
2. **Recordatorios Automáticos:** Puedes agregar scheduled tasks con `@Scheduled`
3. **Plantillas HTML:** Cambiar de `SimpleMailMessage` a `MimeMessage` con HTML

---

**Última actualización:** 2024  
**Proyecto:** Red Solidaria UTP - Enjambre  
**Estado:** ✅ Listo para configurar en Railway
