# ==========================================
# Etapa 1: Build (Compilación y empaquetado)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia de pom.xml y descarga de dependencias en capa cacheada
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia del código fuente y compilación del artefacto ejecutable (los tests corren en CI)
COPY src ./src
RUN mvn clean package -B -Dmaven.test.skip=true

# ==========================================
# Etapa 2: Run (Entorno de ejecución ligero)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Creación de usuario sin privilegios por seguridad
RUN addgroup -S foodhelp && adduser -S foodhelp -G foodhelp
USER foodhelp

# Copia exclusiva del JAR generado desde la etapa de compilación
COPY --from=builder --chown=foodhelp:foodhelp /app/target/*.jar app.jar

# Exposición del puerto HTTP por defecto
EXPOSE 8080

# Healthcheck contra Actuator (requiere spring-boot-starter-actuator y /actuator/health público)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- "http://localhost:${PORT:-8080}/actuator/health" || exit 1

# Arranque con perfil de producción, límites de memoria acordes al contenedor y zona horaria fija
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Duser.timezone=UTC", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
