package com.amarhu.service;

import com.amarhu.video.dto.VideoDTO;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.video.entity.Video;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.repository.VideoRepository;
import com.amarhu.video.service.PersonalVideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PersonalVideoServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private PersonalVideoService personalVideoService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetVideosForRedactor_Success() {
        // Datos simulados para el redactor
        User redactor = new User();
        redactor.setId(1L);
        redactor.setCodigo("RED123");
        redactor.setRole(Role.REDACTOR);

        // Datos simulados para los videos
        Video video1 = new Video();
        video1.setVideoId("v1");
        video1.setTitle("Test Video 1");
        video1.setDescription("Description 1");
        video1.setDate("2023-12-01");
        video1.setMiniatura("miniatura1.png");
        video1.setVisualizaciones(100L);
        video1.setEstimatedRevenue(100.0);
        video1.setEstimatedAdRevenue(20.0); // Campo adicional configurado

        Video video2 = new Video();
        video2.setVideoId("v2");
        video2.setTitle("Test Video 2");
        video2.setDescription("Description 2");
        video2.setDate("2023-12-02");
        video2.setMiniatura("miniatura2.png");
        video2.setVisualizaciones(150L);
        video2.setEstimatedRevenue(200.0);
        video2.setEstimatedAdRevenue(40.0); // Campo adicional configurado

        List<Video> mockVideos = List.of(video1, video2);

        // Simulación de repositorios
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(redactor));
        when(videoRepository.findByCodigo("RED123")).thenReturn(mockVideos);

        // Probar el servicio
        List<VideoDTO> result = personalVideoService.getVideosForRedactor(1L);

        // Verificaciones
        assertEquals(2, result.size());

        VideoDTO videoDTO1 = result.get(0);
        assertEquals("v1", videoDTO1.getVideoId());
        assertEquals("Test Video 1", videoDTO1.getTitle());
        assertEquals(100L, videoDTO1.getVisualizaciones());
        assertEquals(16.6452, videoDTO1.getEstimatedRevenue()); // Aplicar el porcentaje correctamente

        VideoDTO videoDTO2 = result.get(1);
        assertEquals("v2", videoDTO2.getVideoId());
        assertEquals("Test Video 2", videoDTO2.getTitle());
        assertEquals(150L, videoDTO2.getVisualizaciones());
        assertEquals(33.2904, videoDTO2.getEstimatedRevenue()); // Aplicar el porcentaje correctamente
    }
}
