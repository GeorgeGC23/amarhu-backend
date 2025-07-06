# Etapa 1: Build con Maven
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Runtime con JDK Slim
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copia el JAR compilado desde la etapa anterior
COPY --from=build /app/target/amarhu-backend-0.0.1.jar app.jar

# Copia el certificado
COPY src/main/resources/api.pa-reporte.p12 /app/api.pa-reporte.p12

# Comando de inicio con configuración de certificado
ENTRYPOINT ["java", "-Djavax.net.ssl.keyStore=/app/api.pa-reporte.p12", "-Djavax.net.ssl.keyStorePassword=123456", "-Djavax.net.ssl.keyStoreType=PKCS12", "-jar", "app.jar"]
