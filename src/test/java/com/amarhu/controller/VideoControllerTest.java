package com.amarhu.controller;

import com.amarhu.user.security.SecurityConfig;
import com.amarhu.video.controller.VideoController;
import com.amarhu.video.entity.Video;
import com.amarhu.video.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Disabled;

@Disabled
@WebMvcTest(VideoController.class)
@Import({VideoControllerTest.TestConfig.class, SecurityConfig.class})
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VideoService videoService; // Inyectado desde TestConfig

    private String authToken;

    @BeforeEach
    void setUp() {
        this.authToken = authenticateAndGetToken();
    }

    private String authenticateAndGetToken() {
        String email = "dir@example.com";
        String password = "password";

        RestTemplate restTemplate = new RestTemplate();
        String authUrl = "http://localhost:8080/api/auth/login";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, requestBody, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return "Bearer " + response.getBody().get("token");
            } else {
                throw new RuntimeException("Error en autenticación, respuesta inesperada: " + response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el token de autenticación: " + e.getMessage(), e);
        }
    }


    @Test
    public void testGetVideosForDirectivos() throws Exception {
        // Datos simulados de videos
        Video video = new Video();
        video.setVideoId("v1");
        video.setTitle("Test Video");
        video.setDescription("Description");
        video.setDate("2023-12-01");
        video.setEstimatedRevenue(100.0);

        when(videoService.getVideosForDirectivos()).thenReturn(List.of(video));

        // Prueba del endpoint con autenticación
        mockMvc.perform(
                        get("/api/videos")
                                .header(HttpHeaders.AUTHORIZATION, authToken) // Token en la cabecera
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].videoId").value("v1"))
                .andExpect(jsonPath("$[0].title").value("Test Video"))
                .andExpect(jsonPath("$[0].description").value("Description"))
                .andExpect(jsonPath("$[0].date").value("2023-12-01"))
                .andExpect(jsonPath("$[0].estimatedRevenue").value(100.0));
    }

    // Configuración de prueba para inyectar mocks manualmente (en lugar de @MockBean)
    static class TestConfig {
        @Bean
        public VideoService videoService() {
            return Mockito.mock(VideoService.class);
        }
    }
}
