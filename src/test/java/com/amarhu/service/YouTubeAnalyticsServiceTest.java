package com.amarhu.service;

import com.amarhu.video.service.YouTubeAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class YouTubeAnalyticsServiceTest {

    @Mock
    private YouTubeAnalyticsService youTubeAnalyticsService;

    @InjectMocks
    private YouTubeAnalyticsServiceTest analyticsTest;

    private String channelId;
    private String startDate;
    private String endDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        channelId = "UCsT4NSardFSUa0bokXXI6Fg"; // ID del canal
        startDate = "2025-01-01"; // Fecha de inicio
        endDate = "2025-12-31"; // Fecha de fin
    }

    @Test
    void testGetAnalytics() throws Exception {
        // Mockea los datos de entrada y la respuesta
        List<String> videoIds = List.of("videoId1", "videoId2");
        when(youTubeAnalyticsService.getAnalytics(channelId, videoIds, startDate, endDate))
                .thenReturn(List.of(List.of("10.0", "20.0", "100", "50")));

        // Llama al método que quieres testear
        List<List<Object>> analyticsData = youTubeAnalyticsService.getAnalytics(channelId, videoIds, startDate, endDate);

        // Valida que los datos no sean nulos y tengan contenido
        assertNotNull(analyticsData, "Los datos de analítica no deberían ser nulos");
    }
}
