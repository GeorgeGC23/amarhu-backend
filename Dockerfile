# Usa una imagen base de OpenJDK 17
FROM openjdk:21-jdk-slim

# Define una variable para el archivo JAR
ARG JAR_FILE=target/amarhu-backend-0.0.1.jar

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR al contenedor y lo renombra
COPY ${JAR_FILE} app.jar

# Copia el archivo del certificado al contenedor
COPY src/main/resources/api.pa-reporte.p12 /app/api.pa-reporte.p12

# Expone el puerto utilizado por la aplicación
EXPOSE 8443

# Comando para ejecutar la aplicación con el certificado
ENTRYPOINT ["java", "-Djavax.net.ssl.keyStore=/app/api.pa-reporte.p12", "-Djavax.net.ssl.keyStorePassword=123456", "-Djavax.net.ssl.keyStoreType=PKCS12", "-jar", "app.jar"]