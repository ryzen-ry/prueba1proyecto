# 🔍 Diagnóstico de Problemas con Correos en Railway

## Estado del Código ✅

El código Java está correctamente configurado:
- ✅ `@EnableAsync` está habilitado en `EnjambreApplication.java`
- ✅ `AsyncConfig` está configurado con ThreadPoolTaskExecutor
- ✅ `EmailService` tiene todos los métodos para enviar correos
- ✅ `ApiAuthController`, `ApiAdminController` y `ApiDonacionController` usan el EmailService
- ✅ La configuración de SMTP está en `application.properties`

---

## 🚨 Problema Común: Variables de Ambiente No se Leen

Si los correos no se envían a pesar de que las variables están en Railway, el problema es:

**Las variables de ambiente NO se propagaron a la aplicación después de cambiarlas**

### ¿Cómo verificar?

**Opción 1: Usar el endpoint de diagnóstico**

```bash
GET https://tu-app-railway.com/api/debug/email/config
```

**Respuesta si todo está bien:**
```json
{
  "status": "✅ CONFIGURACIÓN DETECTADA",
  "mensaje": "Las variables de ambiente se leyeron correctamente."
}
```

**Respuesta si hay problema:**
```json
{
  "status": "❌ ERROR",
  "error": "spring.mail.username no está configurado"
}
```

---

## 📋 Pasos para Resolver

### Paso 1: Verificar Variables en Railway

1. Ve a [railway.app](https://railway.app)
2. Selecciona tu proyecto
3. Busca la sección "Variables"
4. Confirma que TODAS estas 4 variables existan:

```
MAIL_HOST        ✅
MAIL_PORT        ✅
MAIL_USERNAME    ✅
MAIL_PASSWORD    ✅
```

**SI FALTAN:** Agrégalas ahora

**SI ESTÁN:** Continúa al Paso 2

---

### Paso 2: REDEPLOY la Aplicación

**ESTE ES EL PASO MÁS IMPORTANTE**

Las variables que cambias en Railway no se aplican automáticamente. Necesitas redeplegar:

1. En Railway, ve a "Deployments"
2. Haz click en el botón **"Redeploy Latest"**
3. Espera 10-15 minutos a que termine
4. Verifica en "Status" que diga "Success" (verde)

**⚠️ Si olvidas este paso, los cambios NO se aplicarán**

---

### Paso 3: Prueba el Endpoint de Diagnóstico

Después del redeploy, prueba:

```bash
curl -X POST https://tu-app-railway.com/api/debug/email/test \
  -H "Content-Type: application/json" \
  -d '{"email": "tuemial@example.com"}'
```

**Respuesta esperada:**
```json
{
  "status": "✅ CORREO ENVIADO",
  "email": "tuemial@example.com",
  "instrucciones": [
    "1. Revisa los logs en Railway (Dashboard → Logs)",
    "2. Busca líneas que empiezan con '✅' o '❌'",
    "3. Revisa tu bandeja de entrada (y SPAM)",
    "4. Si ves '❌ ERROR', el mensaje te dirá qué está mal"
  ]
}
```

---

### Paso 4: Revisar los Logs

1. En Railway, ve a "Logs"
2. Busca líneas con:
   - ✅ `✅ Correo de verificación enviado exitosamente`
   - ❌ `❌ ERROR al enviar correo`

**Si ves ✅:** El correo se envió. Revisa tu bandeja de SPAM

**Si ves ❌:** Lee el mensaje de error:
- `AuthenticationFailedException` → Contraseña incorrecta
- `SMTPAuthenticationException` → Email o contraseña mal
- `ConnectionException` → Problema de red (raro)

---

## 🐛 Errores Comunes y Soluciones

### Error: "AuthenticationFailedException"

**Causa:** Tu contraseña de aplicación Gmail es incorrecta

**Solución:**
1. Ve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
2. Genera UNA CONTRASEÑA NUEVA
3. Cópiala SIN espacios
4. Actualiza MAIL_PASSWORD en Railway
5. **REDEPLOY** la aplicación

### Error: "SMTPAuthenticationException"

**Causa:** El email o contraseña no coinciden

**Solución:**
- Verifica que `MAIL_USERNAME` sea tu email Gmail completo (ej: red.solidaria@gmail.com)
- Verifica que `MAIL_PASSWORD` sean 16 caracteres SIN espacios
- **REDEPLOY**

### Error: "No se puede conectar a smtp.gmail.com"

**Causa:** Configuración de puerto incorrecta

**Solución:**
- Verifica `MAIL_PORT` = `587` (exactamente)
- No uses 465, 25 u otros puertos
- **REDEPLOY**

### Recibo el correo pero 5 minutos después

**Causa:** Es normal. El método está marcado con `@Async` (asincrónico)

Los correos se envían en un hilo separado, así que hay un pequeño delay.

---

## 📞 Endpoints para Testing

### 1. Validar Configuración
```
GET /api/debug/email/config
```
Valida que las variables de ambiente se hayan leído

### 2. Enviar Correo de Prueba
```
POST /api/debug/email/test
Content-Type: application/json

{
  "email": "tuemial@example.com"
}
```

### 3. Enviar Correo de Activación
```
POST /api/debug/email/activacion
Content-Type: application/json

{
  "email": "tuemial@example.com"
}
```

---

## ✅ Checklist de Verificación

- [ ] Las 4 variables (MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD) están en Railway
- [ ] He hecho REDEPLOY después de agregar/cambiar las variables
- [ ] El redeploy dice "Success" (verde)
- [ ] Probé el endpoint `/api/debug/email/config` y muestra ✅
- [ ] Probé el endpoint `/api/debug/email/test` y muestra ✅ CORREO ENVIADO
- [ ] Revisé los logs y veo líneas con ✅
- [ ] Recibí el correo en mi bandeja de entrada (o SPAM)

---

## 🎯 Próximos Pasos

Después de que los correos funcionen:

1. **Prueba el registro completo:** Registra una nueva cuenta y deberías recibir el código de verificación
2. **Prueba la activación:** Un admin activa la cuenta y recibirás un correo de bienvenida
3. **Prueba donaciones:** Realiza una donación y recibirás confirmación

---

## 📧 Correos Que Se Envían Automáticamente

| Evento | Método | Correo |
|--------|--------|--------|
| Registro | API registro | Código de verificación |
| Admin activa | `/api/admin/usuarios/{id}/activar` | Confirmación de activación |
| Admin rechaza | `/api/admin/usuarios/{id}/rechazar` | Notificación de rechazo |
| Donación monetaria aprobada | API admin | Confirmación con monto |
| Donación monetaria rechazada | API admin | Notificación de rechazo |
| Donación producto (recojo) | API admin | Coordenadas de recojo |
| Donación producto (sede) | API admin | Datos de la sede |
| Donación producto rechazada | API admin | Notificación de rechazo |

---

**Última actualización:** 2024  
**Proyecto:** Red Solidaria UTP - Enjambre  
**Contacto:** Sistema de diagnóstico automático en `/api/debug/email/*`
