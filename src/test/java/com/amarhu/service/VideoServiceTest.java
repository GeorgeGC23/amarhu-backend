package com.amarhu.service;

import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import com.amarhu.video.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class VideoServiceTest {

    private VideoRepository videoRepository;
    private VideoService videoService;

    @BeforeEach
    public void setup() {
        videoRepository = Mockito.mock(VideoRepository.class);
        videoService = new VideoService(videoRepository); // Inyección manual del mock
    }

    @Test
    public void testGetVideosForDirectivos() {
        LocalDate now = LocalDate.now();
        LocalDate startOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        // Datos simulados
        Video video = new Video();
        video.setVideoId("v1");
        video.setTitle("Test Video");
        video.setDate(now.toString());
        video.setEstimatedRevenue(100.0);

        when(videoRepository.findByDateBetween(startOfPreviousMonth.toString(), now.toString()))
                .thenReturn(List.of(video));

        // Ejecutar servicio
        List<Video> videos = videoService.getVideosForDirectivos();

        // Verificaciones
        assertEquals(1, videos.size());
        assertEquals("v1", videos.get(0).getVideoId());
        assertEquals("Test Video", videos.get(0).getTitle());
    }
}
