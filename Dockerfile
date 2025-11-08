# Etapa 1: Build con Maven
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Runtime con JRE Slim
FROM openjdk:21-jre-slim
WORKDIR /app

# Copia el JAR compilado desde la etapa anterior
COPY --from=build /app/target/amarhu-backend-0.0.1.jar app.jar

EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]