package com.amarhu.video.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtubeAnalytics.v2.YouTubeAnalytics;
import com.google.api.services.youtubeAnalytics.v2.model.QueryResponse;
import com.amarhu.config.YoutubeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeAnalyticsService {

    @Autowired
    private YoutubeConfig youtubeConfig;

    /**
     * Obtiene métricas de los videos desde la API de YouTube Analytics con reintentos (un video por solicitud).
     *
     * @param channelId ID del canal de YouTube
     * @param videoIds  Lista de IDs de videos
     * @return Lista de métricas por video
     * @throws Exception Si ocurre un error al comunicarse con la API después de varios reintentos.
     */
    public List<List<Object>> getAnalytics(String channelId, List<String> videoIds) throws Exception {
        // Obtener credenciales
        Credential credential = youtubeConfig.getFlow().loadCredential("user");
        if (credential == null || credential.getAccessToken() == null || credential.getExpiresInSeconds() <= 0) {
            throw new IllegalStateException("El usuario no está autenticado con Google. Debes iniciar sesión primero.");
        }

        // Usar las credenciales para obtener el servicio de YouTube Analytics
        YouTubeAnalytics analyticsService = youtubeConfig.getYouTubeAnalyticsService(credential);

        // Contenedor para almacenar los resultados de todos los videos
        List<List<Object>> analyticsData = new ArrayList<>();

        // Iterar sobre los IDs de video (un video por solicitud)
        for (String videoId : videoIds) {
            int retryCount = 0;
            int maxRetries = 5; // Define el número máximo de reintentos

            // Definir las fechas de inicio y fin fijas
            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 24);

            // Formatear las fechas
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
            String startDateStr = startDate.format(formatter);
            String endDateStr = endDate.format(formatter);

            System.out.println("Solicitando Analytics para video: " + videoId + " (un video por lote)");

            while (retryCount < maxRetries) {
                try {
                    // Ejecutar la consulta para este video
                    QueryResponse response = analyticsService.reports().query()
                            .setIds("channel==" + channelId)
                            .setMetrics("estimatedRevenue,estimatedAdRevenue,views,averageViewDuration")
                            .setStartDate(startDateStr)
                            .setEndDate(endDateStr)
                            .setFilters("video==" + videoId)
                            .execute();

                    System.out.println("Respuesta de Analytics recibida para video: " + videoId + ". Filas: " + (response != null && response.getRows() != null ? response.getRows().size() : 0));

                    // Extraer filas de datos de la respuesta
                    if (response != null && response.getRows() != null) {
                        analyticsData.addAll(response.getRows());
                    }
                    break; // Si la solicitud es exitosa, sal del bucle de reintento
                } catch (Exception e) {
                    retryCount++;
                    System.err.println("Error al obtener analíticas (intento " + retryCount + "/" + maxRetries + ") para video " + videoId + ": " + e.getMessage());
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(5000); // Esperar 5 segundos antes de reintentar
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new Exception("Reintento interrumpido", ie);
                        }
                    } else {
                        throw new Exception("Error al obtener analíticas después de " + maxRetries + " reintentos para video " + videoId, e);
                    }
                }
            }
            System.out.println("Procesamiento para video " + videoId + " completado.");
            // Opcionalmente, podrías agregar un pequeño Thread.sleep(milliseconds) aquí
            // para evitar ráfagas de solicitudes demasiado rápidas si es necesario.
        }

        System.out.println("Finalizó la obtención de Analytics (un video por lote). Total de filas recibidas: " + analyticsData.size());
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