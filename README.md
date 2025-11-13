# 📨 Notificator

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Automated-blue?logo=docker)
![Jenkins](https://img.shields.io/badge/CI%2FCD-Jenkins-blueviolet?logo=jenkins)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

**Notificator** es una aplicación **Spring Boot** diseñada para centralizar el envío de notificaciones a múltiples canales:  
💬 **Telegram**, 💻 **Discord** y 📧 **correo electrónico (SMTP)**.

El proyecto se compila, genera imagen Docker y se publica automáticamente en **Docker Hub** mediante un **pipeline CI/CD con Jenkins**.  
Está preparado para ejecutarse tanto en servidores **x86** como en **Raspberry Pi (ARM64)**.

---

## 🚀 Características principales

- ✅ API REST desarrollada con **Spring Boot 3 + Java 17**  
- 🤖 Envío de notificaciones a:
  - 💬 **Telegram Bot**
  - 💻 **Discord Webhook**
  - 📧 **Email (SMTP configurable)**
- 🐳 **Dockerfile** optimizado (multi-stage)
- 🔄 **Pipeline Jenkins** para build + push automáticos
- 🌐 Despliegue automático en **Portainer** o **Docker local**
- 💾 Compatible con **ARM64 (Raspberry Pi)** y **AMD64**
- ⚙️ Configuración por variables de entorno

---

## 🧩 Tecnologías

| Tecnología | Uso |
|-------------|-----|
| Java 17 | Lenguaje principal |
| Spring Boot 3.x | Framework backend |
| Spring Mail | Envío de emails |
| Spring Web | Exposición de API REST |
| Maven 3.9 | Build system |
| Docker | Contenedorización |
| Jenkins LTS (JDK17) | CI/CD |
| Telegram Bot API | Notificaciones Telegram |
| Discord Webhook | Notificaciones Discord |

---

## 🏗️ Estructura del proyecto

```
notificator/
├── src/
│   ├── main/java/...        # Código fuente de la app
│   └── main/resources/      # Configuración y plantillas
├── docker/
│   └── Dockerfile           # Imagen de la aplicación
├── Jenkinsfile              # Pipeline CI/CD declarativo
├── pom.xml                  # Dependencias y build
└── README.md                # Documentación del proyecto
```

---

## ⚙️ Configuración local

### 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/jgf78/notificator.git
cd notificator
```

### 2️⃣ Compilar con Maven

```bash
mvn clean install -DskipTests
```

### 3️⃣ Ejecutar la aplicación

```bash
mvn spring-boot:run
```

Por defecto estará disponible en:  
👉 [http://localhost:8081](http://localhost:8081)

---

## 🐳 Uso con Docker

### 🧱 Construir la imagen

```bash
docker build -t jgf78/notificator:latest -f docker/Dockerfile .
```

### ▶️ Ejecutar el contenedor

```bash
docker run -d -p 8083:8081   -e TELEGRAM_BOT_TOKEN=xxxxx   -e TELEGRAM_CHAT_ID=xxxxx   -e DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/...   -e SMTP_HOST=smtp.gmail.com   -e SMTP_PORT=587   -e SMTP_USER=xxxxx@gmail.com   -e SMTP_PASS=xxxxx   --name notificator   jgf78/notificator:latest
```

Aplicación disponible en:  
👉 [http://localhost:8083](http://localhost:8083)

---

## ⚙️ Variables de entorno disponibles

| Variable | Descripción | Valor por defecto |
|-----------|--------------|-------------------|
| `BOOTSTRAP_SERVER` | Servidor Kafka | `192.168.1.3:9092` |
| `TOPIC_DISCORD` | Tópico Kafka para Discord | `discord-messages` |
| `TOPIC_TELEGRAM` | Tópico Kafka para Telegram | `telegram-messages` |
| `TOPIC_MAIL` | Tópico Kafka para email | `mail-messages` |
| `SMTP_SERVER` | Servidor SMTP | `smtp.gmail.com` |
| `SMTP_PORT` | Puerto SMTP | `587` |
| `EMAIL_USERNAME` | Usuario del correo | `julian.rss.android@gmail.com` |
| `EMAIL_PASSWORD` | Contraseña o token de aplicación | `eqcu jplq okul xqzz` |
| `DISCORD_WEBHOOK_URL` | URL del webhook de Discord | *(Requerido)* |
| `TELEGRAM_PROXY_URL` | URL proxy/bot de Telegram | `http://192.168.1.3:8080/...` |
| `TELEGRAM_ID_CHAT` | ID del chat de Telegram | `6610892` |
| `TELEGRAM_ID_GROUP` | ID del grupo de Telegram | `-1001236662890` |
| `EMAIL_TO` | Correo destino de las notificaciones | `julian_gomez_fdez@yahoo.es` |
| `EMAIL_SUBJECT` | Asunto del correo | `Notificación por email` |
| `SERVER_PORT` | Puerto interno de la app | `8081` |
| `SERVER_CONTEXT_PATH` | Context path del servidor | `/api` |
| `LOG_PATH` | Ruta de logs en contenedor | `/var/logs/` |
| `LOG_LEVEL_APP` | Nivel de log de la app | `DEBUG` |
| `LOG_LEVEL_SPRING_BOOT` | Nivel de log de Spring Boot | `INFO` |
| `LOG_LEVEL_SPRING_WEB` | Nivel de log de Spring Web | `INFO` |

---

## 📬 Canales de notificación

### 💬 Telegram

Se requiere un bot creado con **@BotFather** y un chat ID válido.

**Variables necesarias:**
```bash
TELEGRAM_BOT_TOKEN=xxxx
TELEGRAM_CHAT_ID=xxxx
```

---

### 💻 Discord

Envía mensajes a un canal mediante **Discord Webhook**.

**Variables necesarias:**
```bash
DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/XXXXXXXXX
```

---

### 📧 Email (SMTP)

Permite enviar mensajes a través de un servidor SMTP (Gmail, Outlook, etc.).

**Variables necesarias:**
```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=tu_correo@gmail.com
SMTP_PASS=tu_contraseña_o_token_app
```

---

## 🧠 Ejemplo de uso de la API REST

Puedes enviar notificaciones mediante una simple llamada HTTP `POST`.

### Endpoint
```
POST /notify
```

### Ejemplo de cuerpo JSON
```json
{
  "channel": "telegram",
  "message": "¡Hola desde Notificator!"
}
```

### Canales disponibles

| Canal | Valor |
|--------|--------|
| Telegram | `"telegram"` |
| Discord | `"discord"` |
| Email | `"email"` |
| Todos | `"all"` |

### Ejemplo con `curl`

```bash
curl -X POST http://localhost:8081/notify   -H "Content-Type: application/json"   -d '{"channel":"discord", "message":"Mensaje de prueba desde Notificator 🚀"}'
```

---

## 🤖 Pipeline Jenkins

El pipeline CI/CD realiza automáticamente las siguientes tareas:

1. **Checkout** del código desde GitHub  
2. **Build con Maven** (usando contenedor `maven:3.9-eclipse-temurin-17`)  
3. **Creación de la imagen Docker**  
4. **Push a Docker Hub**  
5. *(Opcional)* **Despliegue automático** en Docker local o Portainer

---

### 🔧 Variables de entorno en Jenkins

| Variable | Descripción |
|-----------|-------------|
| `DOCKERHUB_USER` | Usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | Token de acceso a Docker Hub |
| `github-token` | Token personal para integración GitHub |
| `dockerhub` | Credencial Jenkins tipo `usernamePassword` |
| `DEPLOY_HOST` *(opcional)* | Host remoto o local para despliegue |

---

## 🔁 Despliegue automático

Ejemplo de despliegue desde Jenkins o script post-build:

```bash
docker stop notificator || true
docker rm notificator || true
docker pull jgf78/notificator:latest
docker run -d -p 8083:8081 --name notificator jgf78/notificator:latest
```

---

## 📦 Imagen en Docker Hub

👉 [https://hub.docker.com/r/jgf78/notificator](https://hub.docker.com/r/jgf78/notificator)

---

## 🧰 Administración

- 🧭 **Portainer** para gestión visual de contenedores  
- ⚙️ **Jenkins** con plugin *Pipeline: Stage View* para monitorizar cada build  
- 🔄 **Watchtower** (opcional) para actualización automática de contenedores

---

## 👤 Autor

**Julián Gómez Fernández**  
💻 Programador Java  
🐧 Despliegue en Raspberry Pi / Docker  
📦 Docker Hub: [@jgf78](https://hub.docker.com/u/jgf78)  
📬 Integraciones: Telegram · Discord · Email

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**.  
Puedes usarlo, modificarlo y distribuirlo libremente bajo sus términos.

---

> 🧠 “Automatiza, despliega y notifica.  
> Con **Notificator**, tus eventos hablan por sí mismos.” 

