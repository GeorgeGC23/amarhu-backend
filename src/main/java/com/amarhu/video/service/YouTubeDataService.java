package com.amarhu.video.service;

import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YouTubeDataService {

    @Autowired
    private YouTubeVideoService youTubeVideoService;

    @Autowired
    private YouTubeAnalyticsService youTubeAnalyticsService;

    @Autowired
    private VideoRepository videoRepository;

    private static final String CHANNEL_ID = "UCsT4NSardFSUa0bokXXI6Fg"; // ID del canal
    private static final String ANALYTICS_START_DATE = "2025-01-01"; // Referencia para Analytics
    private static final String ANALYTICS_END_DATE = "2025-12-31";   // Referencia para Analytics

    /**
     * Orquesta el flujo para obtener datos de YouTube Data y Analytics.
     */
    public void processYouTubeData() throws Exception {
        // Paso 1: Obtener videos del día
        List<Video> videosFromYouTube = saveOrUpdateVideosFromYouTube();

        // Paso 2: Pasar los IDs de los videos obtenidos a Analytics
        updateAnalyticsForVideos(videosFromYouTube);
    }

    /**
     * Obtiene videos desde YouTube Data y los guarda o actualiza en la base de datos.
     *
     * @return Lista de videos obtenidos desde YouTube Data
     */
    private List<Video> saveOrUpdateVideosFromYouTube() throws Exception {
        // Obtener videos del canal usando YouTubeVideoService
        List<Video> videosFromYouTube = youTubeVideoService.getVideos(CHANNEL_ID);

        for (Video video : videosFromYouTube) {
            Video existingVideo = videoRepository.findById(video.getVideoId()).orElse(null);

            if (existingVideo != null) {
                // Actualiza el video existente
                existingVideo.setTitle(video.getTitle());
                existingVideo.setDescription(video.getDescription());
                existingVideo.setDate(video.getDate());
                existingVideo.setViews(video.getViews());
                existingVideo.setMiniatura(video.getMiniatura());
                videoRepository.save(existingVideo);
            } else {
                // Guarda un nuevo video
                videoRepository.save(video);
            }
        }

        return videosFromYouTube; // Retorna los videos obtenidos del día
    }

    /**
     * Actualiza métricas para los videos obtenidos ese día usando YouTube Analytics.
     *
     * @param videos Lista de videos obtenidos por YouTube Data
     */
    private void updateAnalyticsForVideos(List<Video> videos) throws Exception {
        // Extraer los IDs de los videos obtenidos
        List<String> videoIds = videos.stream().map(Video::getVideoId).toList();

        // Llama a YouTubeAnalyticsService para obtener métricas
        List<List<Object>> analyticsData = youTubeAnalyticsService.getAnalytics(
                CHANNEL_ID,
                videoIds,
                ANALYTICS_START_DATE,
                ANALYTICS_END_DATE
        );

        // Actualiza las métricas en los videos de la base de datos
        for (int i = 0; i < analyticsData.size(); i++) {
            List<Object> analyticsRow = analyticsData.get(i);
            Video video = videos.get(i);

            // Actualiza los datos del video
            video.setEstimatedRevenue((Double) analyticsRow.get(0));
            video.setEstimatedAdRevenue((Double) analyticsRow.get(1));
            video.setViews(((Number) analyticsRow.get(2)).longValue());
            video.setAverageViewDuration(((Number) analyticsRow.get(3)).longValue());
            video.setRpm(calculateRpm((Double) analyticsRow.get(0), ((Number) analyticsRow.get(2)).longValue()));

            // Guarda los cambios
            videoRepository.save(video);
        }
    }

    /**
     * Calcula el RPM basado en ingresos y vistas.
     */
    private double calculateRpm(Double revenue, Long views) {
        return (views == 0) ? 0.0 : (revenue / views) * 1000;
    }
}
