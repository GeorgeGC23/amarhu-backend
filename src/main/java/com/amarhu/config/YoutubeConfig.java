package com.amarhu.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Component
public class YoutubeConfig {

    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/youtube.readonly",
            "https://www.googleapis.com/auth/yt-analytics-monetary.readonly",
            "https://www.googleapis.com/auth/yt-analytics.readonly"
    );

    /**
     * Autentica al usuario y obtiene un token de acceso válido.
     */
    public Credential authorize() throws Exception {
        // Crear el JSON dinámicamente usando variables de entorno
        String clientSecretsJson = String.format("{\n" +
                        "  \"installed\": {\n" +
                        "    \"client_id\": \"%s\",\n" +
                        "    \"project_id\": \"%s\",\n" +
                        "    \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                        "    \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                        "    \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                        "    \"client_secret\": \"%s\",\n" +
                        "    \"redirect_uris\": [\"http://localhost:8008\"]\n" +
                        "  }\n" +
                        "}",
                System.getenv("GOOGLE_CLIENT_ID"),
                System.getenv("GOOGLE_PROJECT_ID"),
                System.getenv("GOOGLE_CLIENT_SECRET")
        );

        // Convertir el JSON dinámico a un InputStream
        ByteArrayInputStream clientSecretsStream = new ByteArrayInputStream(clientSecretsJson.getBytes());

        // Cargar las credenciales
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(clientSecretsStream));

        // Crear flujo de autorización
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        ).setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY_PATH).toFile()))
                .setAccessType("offline")
                .build();

        // Intentar cargar credenciales existentes
        Credential credential = flow.loadCredential("user");
        if (credential == null || credential.getAccessToken() == null || credential.getExpiresInSeconds() <= 0) {
            // Si no existen credenciales válidas, abrir el flujo de autorización en el navegador
            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder()
                    .setPort(8008) // Puerto para escuchar la respuesta de Google
                    .build()
            ).authorize("user");
        }

        return credential;
    }

    /**
     * Crea y devuelve un cliente YouTube autorizado.
     */
    public YouTube getYouTubeService() throws Exception {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                authorize()
        ).setApplicationName("AmarhuBackend")
                .build();
    }

    /**
     * Crea y devuelve un cliente YouTubeAnalytics autorizado.
     */
    public YouTubeAnalytics getYouTubeAnalyticsService() throws Exception {
        return new YouTubeAnalytics.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                authorize()
        ).setApplicationName("AmarhuBackend")
                .build();
    }
}
