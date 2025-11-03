# Imagen base compatible multi-arquitectura
FROM eclipse-temurin:21-jdk

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR al contenedor
COPY target/notificator-0.0.1-SNAPSHOT.jar app.jar

# Puerto expuesto (ajústalo si tu app usa otro)
EXPOSE 8080

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
