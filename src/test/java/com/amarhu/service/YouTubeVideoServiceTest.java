package com.amarhu.service;

import com.amarhu.video.entity.Video;
import com.amarhu.video.service.YouTubeVideoService;
import com.google.api.services.youtube.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class YouTubeVideoServiceTest {

    @Mock
    private YouTubeVideoService youTubeVideoService;

    @InjectMocks
    private YouTubeVideoServiceTest serviceTest;

    private String channelId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        channelId = "UCsT4NSardFSUa0bokXXI6Fg"; // ID del canal
    }

    @Test
    void testGetVideos() throws Exception {
        // Mockea la respuesta del servicio
        when(youTubeVideoService.getVideos(channelId)).thenReturn(List.of(new Video()));

        // Llama al método que quieres testear
        List<Video> videos = youTubeVideoService.getVideos(channelId);

        // Valida que la lista no sea nula y contenga elementos
        assertNotNull(videos, "La lista de videos no debería ser nula");
    }
}
