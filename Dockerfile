# Usa una imagen base de OpenJDK 21
FROM openjdk:17-jdk-slim

# Define una variable para el archivo JAR
ARG JAR_FILE=target/amarhu-backend-0.0.1.jar

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR al contenedor y lo renombra
COPY ${JAR_FILE} app.jar

# Expone el puerto utilizado por la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]