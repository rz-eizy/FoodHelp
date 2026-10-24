# ==========================================
# Etapa 1: Build (Compilación y empaquetado)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copia de pom.xml y descarga de dependencias en capa cacheada
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia del código fuente y compilación del artefacto ejecutable
COPY src ./src
RUN mvn clean package -DskipTests

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

# Comando de arranque inyectando el perfil de producción
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
