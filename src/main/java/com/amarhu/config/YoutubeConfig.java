package com.amarhu.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
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

    private final GoogleAuthorizationCodeFlow flow;

    public YoutubeConfig() throws Exception {
        String clientSecretsJson = String.format("{\n" +
                        "  \"web\": {\n" +
                        "    \"client_id\": \"%s\",\n" +
                        "    \"project_id\": \"%s\",\n" +
                        "    \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                        "    \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                        "    \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                        "    \"client_secret\": \"%s\",\n" +
                        "    \"redirect_uris\": [\"https://api.pa-reporte.com/oauth2/callback\"]\n" +
                        "  }\n" +
                        "}",
                System.getenv("GOOGLE_CLIENT_ID"),
                System.getenv("GOOGLE_PROJECT_ID"),
                System.getenv("GOOGLE_CLIENT_SECRET")
        );

        ByteArrayInputStream clientSecretsStream = new ByteArrayInputStream(clientSecretsJson.getBytes());
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(clientSecretsStream));

        this.flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        ).setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY_PATH).toFile()))
                .setAccessType("offline")
                .build();
    }

    public GoogleAuthorizationCodeFlow getFlow() {
        return flow;
    }

    /**
     * Obtiene las credenciales si ya existen, o solicita una nueva autenticación.
     */
    public Credential authorize(String authCode) throws Exception {
        if (authCode == null || authCode.isEmpty()) {
            throw new IllegalArgumentException("Código de autenticación inválido. Debes iniciar sesión primero.");
        }

        return flow.createAndStoreCredential(
                flow.newTokenRequest(authCode)
                        .setRedirectUri("https://api.pa-reporte.com/oauth2/callback")
                        .execute(),
                "user"
        );
    }

    /**
     * Genera la URL de autenticación de Google.
     */
    public String getAuthUrl() {
        return flow.newAuthorizationUrl()
                .setRedirectUri("https://api.pa-reporte.com/oauth2/callback")
                .build();
    }

    /**
     * Crea y devuelve un cliente YouTube autorizado.
     */
    public YouTube getYouTubeService(Credential credential) throws Exception {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName("AmarhuBackend")
                .build();
    }

    /**
     * Crea y devuelve un cliente YouTubeAnalytics autorizado.
     */
    public YouTubeAnalytics getYouTubeAnalyticsService(Credential credential) throws Exception {
        return new YouTubeAnalytics.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName("AmarhuBackend")
                .build();
    }
}
