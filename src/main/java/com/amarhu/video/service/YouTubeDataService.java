package com.amarhu.video.service;

import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YouTubeDataService {

    @Autowired
    private YouTubeVideoService youTubeVideoService;

    @Autowired
    private YouTubeAnalyticsService youTubeAnalyticsService;

    @Autowired
    private VideoRepository videoRepository;

    private static final String CHANNEL_ID = "UCsT4NSardFSUa0bokXXI6Fg"; // ID del canal

    /**
     * Orquesta el flujo para obtener datos de YouTube Data y Analytics.
     */
    public void processYouTubeData() throws Exception {
        // Paso 1: Obtener videos del día
        List<Video> videosFromYouTube = saveOrUpdateVideosFromYouTube();
        System.out.println("Número de videos obtenidos de YouTube Data: " + videosFromYouTube.size());

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
        System.out.println("Tamaño de la lista de videos obtenida de YouTubeVideoService: " + videosFromYouTube.size());

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
                System.out.println("Video actualizado: " + video.getVideoId());
            } else {
                // Guarda un nuevo video
                videoRepository.save(video);
                System.out.println("Video guardado: " + video.getVideoId());
            }
        }

        System.out.println("Finalizó saveOrUpdateVideosFromYouTube. Retornando " + videosFromYouTube.size() + " videos.");
        return videosFromYouTube; // Retorna los videos obtenidos del día
    }

    /**
     * Actualiza métricas para los videos obtenidos ese día usando YouTube Analytics.
     *
     * @param videos Lista de videos obtenidos por YouTube Data
     */
    private void updateAnalyticsForVideos(List<Video> videos) throws Exception {
        List<String> videoIds = videos.stream().map(Video::getVideoId).collect(Collectors.toList());
        System.out.println("Número de Video IDs para Analytics: " + videoIds.size());

        List<List<Object>> analyticsData = youTubeAnalyticsService.getAnalytics(
                CHANNEL_ID,
                videoIds
        );
        System.out.println("Número de filas de datos de Analytics recibidas: " + analyticsData.size());
        System.out.println("Contenido de analyticsData (primeros 5 filas si existen):");
        for (int i = 0; i < Math.min(5, analyticsData.size()); i++) {
            System.out.println("Fila " + i + ": " + analyticsData.get(i));
        }

        for (int i = 0; i < analyticsData.size(); i++) {
            List<Object> analyticsRow = analyticsData.get(i);
            Video video = videos.get(i);

            System.out.println("Procesando datos de Analytics para video: " + video.getVideoId());
            System.out.println("Fila de Analytics: " + analyticsRow);

            Double estimatedRevenue = null;
            Double estimatedAdRevenue = null;
            Long views = null;
            Long averageViewDuration = null;

            try {
                estimatedRevenue = (analyticsRow.get(0) instanceof BigDecimal) ?
                        ((BigDecimal) analyticsRow.get(0)).doubleValue() : (Double) analyticsRow.get(0);
                estimatedAdRevenue = (analyticsRow.get(1) instanceof BigDecimal) ?
                        ((BigDecimal) analyticsRow.get(1)).doubleValue() : (Double) analyticsRow.get(1);
                views = ((Number) analyticsRow.get(2)).longValue();
                averageViewDuration = ((Number) analyticsRow.get(3)).longValue();

                video.setEstimatedRevenue(estimatedRevenue);
                video.setEstimatedAdRevenue(estimatedAdRevenue);
                video.setViews(views);
                video.setAverageViewDuration(averageViewDuration);
                video.setRpm(calculateRpm(estimatedRevenue, views));

                videoRepository.save(video);
                System.out.println("Datos de Analytics guardados para video: " + video.getVideoId() + " - Revenue: " + estimatedRevenue + ", Views: " + views + ", Duration: " + averageViewDuration);

            } catch (Exception e) {
                System.err.println("Error al procesar o guardar datos de Analytics para video " + video.getVideoId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        // Al final de updateAnalyticsForVideos()
        System.out.println("Finalizó la actualización de Analytics para " + videos.size() + " videos.");
    }

    /**
     * Calcula el RPM basado en ingresos y vistas.
     */
    private double calculateRpm(Double revenue, Long views) {
        return (views == 0) ? 0.0 : (revenue / views) * 1000;
    }
}