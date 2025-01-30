package com.amarhu.video.service;

import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import com.amarhu.config.YoutubeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeAnalyticsService {

    @Autowired
    private YoutubeConfig youtubeConfig;

    /**
     * Obtiene métricas de los videos desde la API de YouTube Analytics.
     *
     * @param channelId ID del canal de YouTube
     * @param videoIds  Lista de IDs de videos
     * @param startDate Fecha de inicio en formato "YYYY-MM-DD"
     * @param endDate   Fecha de fin en formato "YYYY-MM-DD"
     * @return Lista de métricas por video
     * @throws Exception Si ocurre un error al comunicarse con la API
     */
    public List<List<Object>> getAnalytics(String channelId, List<String> videoIds, String startDate, String endDate) throws Exception {
        YouTubeAnalytics analyticsService = youtubeConfig.getYouTubeAnalyticsService();

        // Contenedor para almacenar los resultados de todos los videos
        List<List<Object>> analyticsData = new ArrayList<>();

        // Iterar sobre los IDs de video en lotes para evitar límites de API
        int batchSize = 50; // Límite de la API de Analytics
        for (int i = 0; i < videoIds.size(); i += batchSize) {
            List<String> batch = videoIds.subList(i, Math.min(videoIds.size(), i + batchSize));

            // Ejecutar la consulta para este lote
            QueryResponse response = analyticsService.reports().query()
                    .setIds("channel==" + channelId)
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setMetrics("estimatedRevenue,estimatedAdRevenue,views,averageViewDuration")
                    .setFilters("video==" + String.join(",", batch))
                    .execute();

            // Extraer filas de datos de la respuesta
            if (response != null && response.getRows() != null) {
                analyticsData.addAll(response.getRows());
            }
        }

        return analyticsData;
    }

    /**
     * Procesa los datos de métricas y calcula RPM para cada video.
     *
     * @param analyticsData Datos crudos obtenidos de la API de Analytics
     * @return Lista de objetos procesados con métricas y RPM
     */
    public List<VideoMetrics> processAnalyticsData(List<List<Object>> analyticsData) {
        List<VideoMetrics> processedMetrics = new ArrayList<>();

        for (List<Object> row : analyticsData) {
            Double estimatedRevenue = (Double) row.get(0);
            Double estimatedAdRevenue = (Double) row.get(1);
            Long views = ((Number) row.get(2)).longValue();
            Long averageViewDuration = ((Number) row.get(3)).longValue();

            // Calcular RPM
            Double rpm = (views > 0) ? (estimatedRevenue / views) * 1000 : 0.0;

            // Crear objeto de métricas procesadas
            VideoMetrics metrics = new VideoMetrics(estimatedRevenue, estimatedAdRevenue, views, averageViewDuration, rpm);
            processedMetrics.add(metrics);
        }

        return processedMetrics;
    }

    /**
     * Clase interna para encapsular métricas de video con RPM.
     */
    public static class VideoMetrics {
        private final Double estimatedRevenue;
        private final Double estimatedAdRevenue;
        private final Long views;
        private final Long averageViewDuration;
        private final Double rpm;

        public VideoMetrics(Double estimatedRevenue, Double estimatedAdRevenue, Long views, Long averageViewDuration, Double rpm) {
            this.estimatedRevenue = estimatedRevenue;
            this.estimatedAdRevenue = estimatedAdRevenue;
            this.views = views;
            this.averageViewDuration = averageViewDuration;
            this.rpm = rpm;
        }

        public Double getEstimatedRevenue() {
            return estimatedRevenue;
        }

        public Double getEstimatedAdRevenue() {
            return estimatedAdRevenue;
        }

        public Long getViews() {
            return views;
        }

        public Long getAverageViewDuration() {
            return averageViewDuration;
        }

        public Double getRpm() {
            return rpm;
        }
    }
}
