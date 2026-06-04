# 🤝 Red Solidaria UTP - Enjambre

Plataforma de voluntariado y apoyo a personas discapacitadas.

## 🚀 Inicio Rápido

### Requisitos
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Instalación Local

```bash
# Clonar el proyecto
git clone https://github.com/tu-usuario/prueba1proyecto.git
cd prueba1proyecto

# Compilar y ejecutar
mvn spring-boot:run
```

---

## 📧 Configuración de Correos en Railway

### Paso 1: Generar Contraseña de Aplicación Gmail

1. Ve a [myaccount.google.com](https://myaccount.google.com)
2. Habilita "Verificación en 2 pasos"
3. Ve a [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
4. Genera una contraseña y cópiala **SIN espacios**

### Paso 2: Agregar Variables en Railway

En tu dashboard de Railway, agrega estas 4 variables:

```
MAIL_HOST        = smtp.gmail.com
MAIL_PORT        = 587
MAIL_USERNAME    = tu_correo@gmail.com
MAIL_PASSWORD    = tu_contrasena_de_16_caracteres_sin_espacios
```

### Paso 3: REDEPLOY

**⚠️ IMPORTANTE:** Haz click en "Redeploy Latest" después de cambiar las variables

### Paso 4: Verificar

Prueba el endpoint de diagnóstico:
```bash
curl -X POST https://tu-app.railway.app/api/debug/email/test \
  -H "Content-Type: application/json" \
  -d '{"email": "prueba@gmail.com"}'
```

---

## 📍 Cambios Recientes

### Iconos del Mapa (✅ Completado)
- Voluntarios: **Flecha Azul** (↑)
- Personas Discapacitadas: **Flecha Roja** (↑)

### Sistema de Correos (✅ Configurado)
- Códigos de verificación ✅
- Confirmación de activación ✅
- Notificaciones de donaciones ✅
- Endpoints de diagnóstico en `/api/debug/email/*`

---

## 📚 Documentación

- [GUIA_CONFIGURACION_CORREOS_RAILWAY.md](./GUIA_CONFIGURACION_CORREOS_RAILWAY.md) - Guía paso a paso
- [DIAGNOSTICO_CORREOS.md](./DIAGNOSTICO_CORREOS.md) - Solución de problemas

---

## 🏗️ Estructura del Proyecto

```
src/main/
├── java/com/redsolidaria/enjambre/
│   ├── controller/          # Controladores MVC y REST
│   ├── api/                 # Endpoints de API
│   ├── service/             # Lógica de negocio
│   ├── model/               # Entidades JPA
│   ├── repository/          # Acceso a datos
│   ├── config/              # Configuración de Spring
│   └── ws/                  # WebSocket
└── resources/
    ├── templates/           # Thymeleaf HTML
    ├── static/              # CSS, JS, vendor libs
    └── application.properties
```

---

## ⚙️ Endpoints Principales

### Autenticación
- `POST /api/auth/login`
- `POST /api/auth/registro/voluntario`
- `POST /api/auth/registro/discapacitado`
- `POST /api/auth/verificar-codigo`

### Admin
- `GET /api/admin/dashboard`
- `POST /api/admin/usuarios/{id}/activar`
- `POST /api/admin/usuarios/{id}/rechazar`

### Donaciones
- `POST /api/donaciones/monetaria/guardar-temporal`
- `POST /api/donaciones/productos/guardar`

### Debug (Desarrollo)
- `GET /api/debug/email/config` - Verificar configuración
- `POST /api/debug/email/test` - Enviar correo de prueba

---

## 🐛 Solución de Problemas

### Los correos no se envían

1. Verifica `/api/debug/email/config`
2. Revisa los logs en Railway
3. Asegúrate de haber hecho REDEPLOY después de cambiar variables
4. Lee [DIAGNOSTICO_CORREOS.md](./DIAGNOSTICO_CORREOS.md)

### Error de autenticación en correos

- La contraseña de aplicación Gmail es incorrecta
- Genera una nueva en [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
- Actualiza MAIL_PASSWORD en Railway sin espacios
- REDEPLOY

---

## 📝 Licencia

Este proyecto es parte de la iniciativa Red Solidaria UTP.

---

## 👥 Equipo

Proyecto desarrollado para la Universidad Tecnológica del Perú (UTP)