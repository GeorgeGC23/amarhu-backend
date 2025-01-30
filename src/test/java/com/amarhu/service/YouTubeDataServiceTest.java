package com.amarhu.service;

import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import com.amarhu.video.service.YouTubeAnalyticsService;
import com.amarhu.video.service.YouTubeDataService;
import com.amarhu.video.service.YouTubeVideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class YouTubeDataServiceTest {

    @InjectMocks
    private YouTubeDataService youTubeDataService;

    @Mock
    private YouTubeVideoService youTubeVideoService;

    @Mock
    private YouTubeAnalyticsService youTubeAnalyticsService;

    @Mock
    private VideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessYouTubeData() throws Exception {
        // Simula videos obtenidos de YouTubeVideoService
        Video video1 = new Video();
        video1.setVideoId("video1");
        video1.setTitle("Title1");
        video1.setDescription("Description1");
        video1.setDate("2025-01-01T10:00:00Z");
        video1.setViews(100L);
        video1.setEstimatedRevenue(0.0);
        video1.setEstimatedAdRevenue(0.0);
        video1.setRpm(10.0);
        video1.setMiniatura("miniatura1");

        Video video2 = new Video();
        video2.setVideoId("video2");
        video2.setTitle("Title2");
        video2.setDescription("Description2");
        video2.setDate("2025-01-02T10:00:00Z");
        video2.setViews(200L);
        video2.setEstimatedRevenue(0.0);
        video2.setEstimatedAdRevenue(0.0);
        video2.setRpm(20.0);
        video2.setMiniatura("miniatura2");

        List<Video> videosFromYouTube = Arrays.asList(video1, video2);

        // Simula métricas obtenidas de YouTubeAnalyticsService
        List<List<Object>> analyticsData = Arrays.asList(
                Arrays.asList(10.0, 5.0, 100L, 120L), // Métricas para video1
                Arrays.asList(20.0, 10.0, 200L, 240L) // Métricas para video2
        );

        // Configura los mocks
        when(youTubeVideoService.getVideos(anyString())).thenReturn(videosFromYouTube);
        when(youTubeAnalyticsService.getAnalytics(anyString(), anyList(), anyString(), anyString())).thenReturn(analyticsData);

        // Simula que los videos ya existen en la base de datos
        when(videoRepository.findById("video1")).thenReturn(java.util.Optional.of(video1));
        when(videoRepository.findById("video2")).thenReturn(java.util.Optional.of(video2));
        when(videoRepository.findAll()).thenReturn(videosFromYouTube);

        // Ejecuta el método a probar
        youTubeDataService.processYouTubeData();

        // Verifica que se llamaron los métodos correctos
        verify(youTubeVideoService, times(1)).getVideos(anyString());
        verify(youTubeAnalyticsService, times(1)).getAnalytics(anyString(), anyList(), anyString(), anyString());
        verify(videoRepository, times(4)).save(any(Video.class));


        // Valida los datos actualizados en video1
        assertEquals(10.0, video1.getEstimatedRevenue());
        assertEquals(100L, video1.getViews());
        assertEquals(120L, video1.getAverageViewDuration());
        assertEquals(100.0, video1.getRpm());

        // Valida los datos actualizados en video2
        assertEquals(20.0, video2.getEstimatedRevenue());
        assertEquals(200L, video2.getViews());
        assertEquals(240L, video2.getAverageViewDuration());
        assertEquals(100.0, video2.getRpm());
    }

}
