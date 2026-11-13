
# Imagen base compatible multi-arquitectura
FROM eclipse-temurin:21-jdk

# Etiquetas de metadatos
LABEL module.name="notificator" \
      module.maintainer="julian.rss.android@gmail.com"

# Directorio de trabajo
WORKDIR /app

# Copia del JAR
COPY target/notificator-0.0.1-SNAPSHOT.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Configuración de memoria JVM
ENV JVM_INITIAL_JAVA_HEAP=128m
ENV JVM_MAX_JAVA_HEAP=768m
ENV JVM_MAX_MEMORY=1000m

# Opciones JVM
ENV JAVA_OPTS="-Xms${JVM_INITIAL_JAVA_HEAP} \
    -Xmx${JVM_MAX_JAVA_HEAP} \
    -XX:MaxRAM=${JVM_MAX_MEMORY} \
    -XX:-TieredCompilation \
    -XX:ReservedCodeCacheSize=120m \
    -XX:+UnlockExperimentalVMOptions \
    --add-opens java.base/java.io=ALL-UNNAMED \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8"

# Arranque de Spring Boot
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=docker -jar /app/app.jar"]
