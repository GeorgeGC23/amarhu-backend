package com.amarhu.video.controller;

import com.amarhu.config.YoutubeConfig;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")

public class YouTubeAuthController {

    @Autowired
    private YoutubeConfig youtubeConfig;

    /**
     * Endpoint para obtener la URL de autenticación de Google.
     *
     * @return URL para iniciar sesión en Google
     */
    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> getAuthUrl() {
        String authUrl = youtubeConfig.getAuthUrl();
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Callback de Google OAuth2. Recibe el código de autorización y obtiene el token de acceso.
     *
     * @param code Código de autorización recibido de Google
     * @return Mensaje de éxito o error
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> handleOAuth2Callback(@RequestParam("code") String code) {
        Map<String, String> response = new HashMap<>();
        try {
            // Obtener credenciales usando el código de autorización
            Credential credential = youtubeConfig.authorize(code);

            response.put("message", "Autenticación exitosa");
            response.put("accessToken", credential.getAccessToken());
            response.put("refreshToken", credential.getRefreshToken());
            response.put("expiresIn", String.valueOf(credential.getExpirationTimeMilliseconds()));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", "Código de autenticación inválido: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("error", "Error al autenticar: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
