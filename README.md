# 📨 Notificator

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Automated-blue?logo=docker)
![Jenkins](https://img.shields.io/badge/CI%2FCD-Jenkins-blueviolet?logo=jenkins)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

**Notificator** es una aplicación **Spring Boot** diseñada para centralizar el envío de notificaciones a múltiples canales:  
💬 **Telegram**, 💬 **Whatsapp**, 💻 **Discord**, 💻 **MQTT**, 📧 **correo electrónico (SMTP) y mensaje programado a cualquiera de los anteriores canales**.

El proyecto se compila, genera imagen Docker y se publica automáticamente en **Docker Hub** mediante un **pipeline CI/CD con Jenkins**.  
Está preparado para ejecutarse tanto en servidores **x86** como en **Raspberry Pi (ARM64)**.

---

## 🚀 Características principales

- ✅ API REST desarrollada con **Spring Boot 3 + Java 17**  
- 🤖 Envío de notificaciones a:
  - 💬 **Telegram Bot**
  - 💬 **Whatsapp**
  - 💻 **Discord Webhook**
  - 💻 **MQTT**
  - 📧 **Email (SMTP configurable)**
  - 💬 **Envío programado de un mensaje diario a cualquiera de los anteriores canales**
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
docker run -d -p 8083:8081   -e TELEGRAM_BOT_TOKEN=xxxxx   -e TELEGRAM_CHAT_ID=xxxxx   -e DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/...   -e SMTP_HOST=smtp.gmail.com   -e SMTP_PORT=587   -e SMTP_USER=xxxxx@gmail.com   -e SMTP_PASS=xxxxx   
-e WHATSAPP_APIKEY=xxxxx -e WHATSAPP_TYPE=xxxxx --name notificator   
-e MQTT_BROKER=xxxxx  -e MQTT_TOPIC=xxxxx -e MQTT_CLIENT_ID=xxxxx -e MQTT_QOS=xxxxx
jgf78/notificator:latest
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
| `EMAIL_USERNAME` | Usuario del correo | `xxxx@xxxx.xxx` |
| `EMAIL_PASSWORD` | Contraseña o token de aplicación | `xxxxxxxxx` |
| `DISCORD_WEBHOOK_URL` | URL del webhook de Discord | *(Requerido)* |
| `TELEGRAM_PROXY_URL` | URL proxy/bot de Telegram | `http://192.168.1.3:8080/...` |
| `TELEGRAM_ID_CHAT` | ID del chat de Telegram | `xxxxxxx` |
| `TELEGRAM_ID_GROUP` | ID del grupo de Telegram | `-xxxxxx` |
| `EMAIL_TO` | Correo destino de las notificaciones | `xxxxx@xxxc.xxx` |
| `EMAIL_SUBJECT` | Asunto del correo | `Notificación por email` |
| `WHATSAPP_APIKEY` | Apikey | `xxxxx` |
| `WHATSAPP_TYPE` | Tipo de mensaje | `alarm_notification` |
| `MQTT_BROKER` | Broker MQTT | `tcp://broker.hivemq.com:1883` |
| `MQTT_TOPIC` | Topic MQTT | `notificator/alerts` |
| `MQTT_CLIENT_ID` | ClientId MQTT | `notificator-app` |
| `MQTT_QOS` | QOS MQTT | `1` |
| `SCHEDULER_ENABLED` | Activado | `true` |
| `SCHEDULER_MESSAGE` | Mensaje a enviar | `Buenos días, ` |
| `SCHEDULER_HOUR` | Hora | `07` |
| `SCHEDULER_MINUTE` | Minuto | `00` |
| `SCHEDULER_ZONE` | Zona horaria | `Europe/Madrid` |
| `SCHEDULER_SERVICE` | Servicio de envío | `telegramServiceImpl` |
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

### 💬 Whatsapp

Se requiere un bot registro previo en **https://inout.bot/whatsapp-api/** donde obtendras tu Apikey.

**Variables necesarias:**
```bash
WHATSAPP_APIKEY=xxxx
WHATSAPP_TYPE=xxxx
```

---

### 💻 Discord

Envía mensajes a un canal mediante **Discord Webhook**.

**Variables necesarias:**
```bash
DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/xxxx
```

---

### 💻 MQTT

Envía mensajes a un cliente MQTT.

**Variables necesarias:**
```bash
MQTT_BROKER=xxxx
MQTT_TOPIC=xxxx
MQTT_CLIENT_ID=xxxx
MQTT_QOS=xxxx
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

### 💬 Mensaje programado

Permite enviar un mensaje programado a cualquiera de los canales anteriores.

**Variables necesarias:**
```bash
SCHEDULER_ENABLED=true/false
SCHEDULER_MESSAGE=Mensaje a enviar
SCHEDULER_HOUR=Hora
SCHEDULER_MINUTE=Minuto
SCHEDULER_ZONE=Zona horaria
SCHEDULER_SERVICE=Servicio al que se quiere mandar el mensaje (Telegram, Discord, etc...)
```

---

## 🧠 Ejemplo de uso de la API REST

Puedes enviar notificaciones mediante una simple llamada HTTP `POST`.

### Endpoint
```
POST /api/messages/send
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
| Whatsapp | `"whatsapp"` |
| MQTT | `"mqtt"` |
| Todos | `"all"` |

### Ejemplo con `curl`

```bash
curl -X POST http://localhost:8081/api/messages/send   -H "Content-Type: application/json"   -d '{"channel":"discord", "message":"Mensaje de prueba desde Notificator 🚀"}'
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
