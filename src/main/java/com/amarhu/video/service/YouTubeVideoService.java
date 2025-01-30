package com.amarhu.video.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.amarhu.config.YoutubeConfig;
import com.amarhu.video.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class YouTubeVideoService {

    @Autowired
    private YoutubeConfig youtubeConfig;

    /**
     * Obtiene videos desde la API de YouTube según las condiciones de fechas y lógica diaria.
     *
     * @param channelId ID del canal de YouTube.
     * @return Lista de videos obtenidos desde la API.
     * @throws Exception Si ocurre un error al comunicarse con la API.
     */
    public List<Video> getVideos(String channelId) throws Exception {
        YouTube youtubeService = youtubeConfig.getYouTubeService();

        // Lógica de fechas basada en las condiciones
        LocalDate now = LocalDate.now();
        LocalDate startOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate endOfCurrentMonth = now.withDayOfMonth(now.lengthOfMonth());
        LocalDate endWithExtraDays = endOfCurrentMonth.plusDays(7);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        String startDate;
        String endDate;

        // Condiciones para los primeros días del mes
        if (now.getDayOfMonth() <= 2) {
            // Si es día 1 o 2, pide únicamente datos del mes pasado
            LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1).withDayOfMonth(1);
            LocalDate endOfLastMonth = startOfCurrentMonth.minusDays(1);
            startDate = startOfLastMonth.format(formatter) + "T00:00:00Z";
            endDate = endOfLastMonth.format(formatter) + "T23:59:59Z";
        } else if (now.getDayOfMonth() <= 7) {
            // Si es entre día 3 y 7, pide datos del mes pasado y los primeros días del mes actual
            LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1).withDayOfMonth(1);
            LocalDate endOfLastMonth = startOfCurrentMonth.minusDays(1);
            startDate = startOfLastMonth.format(formatter) + "T00:00:00Z";
            endDate = now.minusDays(2).format(formatter) + "T23:59:59Z";
        } else {
            // Para el resto del mes, pide datos solo del mes actual hasta el día anterior
            startDate = startOfCurrentMonth.format(formatter) + "T00:00:00Z";
            endDate = now.minusDays(2).format(formatter) + "T23:59:59Z";
        }

        // Llamada a la API de YouTube para obtener los videos en el rango de fechas
        return fetchVideosFromYouTube(youtubeService, channelId, startDate, endDate);
    }

    /**
     * Realiza la solicitud a la API de YouTube y devuelve los videos obtenidos.
     */
    private List<Video> fetchVideosFromYouTube(YouTube youtubeService, String channelId, String startDate, String endDate) throws Exception {
        List<SearchResult> searchResults = youtubeService.search()
                .list(Collections.singletonList("id,snippet"))
                .setChannelId(channelId)
                .setPublishedAfter(startDate)
                .setPublishedBefore(endDate)
                .setType(Collections.singletonList("video"))
                .setMaxResults(50L)
                .execute()
                .getItems();

        List<Video> videos = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            String videoId = searchResult.getId().getVideoId();

            // Obtén detalles del video (snippet y estadísticas)
            VideoListResponse videoResponse = youtubeService.videos()
                    .list(Collections.singletonList("snippet,statistics"))
                    .setId(Collections.singletonList(videoId))
                    .execute();

            if (videoResponse.getItems().isEmpty()) {
                continue;
            }

            com.google.api.services.youtube.model.Video youtubeVideo = videoResponse.getItems().get(0);
            VideoSnippet snippet = youtubeVideo.getSnippet();

            // Mapea los datos de YouTube al modelo de tu entidad
            Video video = new Video();
            video.setVideoId(videoId);
            video.setTitle(snippet.getTitle());
            video.setDescription(snippet.getDescription());
            video.setDate(snippet.getPublishedAt().toStringRfc3339());
            video.setViews(youtubeVideo.getStatistics().getViewCount().longValue());
            video.setVisualizaciones(youtubeVideo.getStatistics().getViewCount().longValue());
            video.setEstimatedRevenue(0.0); // Analytics llenará este dato más tarde

            // Calcular RPM (Revenue per Mille) si es necesario
            Double revenue = 0.0; // Placeholder, será actualizado con Analytics
            Long views = youtubeVideo.getStatistics().getViewCount().longValue();
            video.setRpm(calculateRpm(revenue, views));

            videos.add(video);
        }

        return videos;
    }

    /**
     * Calcula el RPM basado en los ingresos y vistas.
     *
     * @param revenue Ingresos estimados.
     * @param views   Número de vistas.
     * @return RPM calculado.
     */
    private double calculateRpm(Double revenue, Long views) {
        if (views == 0) {
            return 0.0;
        }
        return (revenue / views) * 1000;
    }
}
