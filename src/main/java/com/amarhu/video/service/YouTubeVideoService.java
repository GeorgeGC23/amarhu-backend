package com.amarhu.video.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.amarhu.config.YoutubeConfig;
import com.amarhu.video.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class YouTubeVideoService {

    @Autowired
    private YoutubeConfig youtubeConfig;

    public List<Video> getVideos(String channelId) throws Exception {
        Credential credential = youtubeConfig.getFlow().loadCredential("user");
        if (credential == null || credential.getAccessToken() == null || credential.getExpiresInSeconds() <= 0) {
            throw new IllegalStateException("El usuario no está autenticado con Google. Debes iniciar sesión primero.");
        }

        YouTube youtubeService = youtubeConfig.getYouTubeService(credential);

        LocalDate now = LocalDate.now();
        LocalDate startOfCurrentMonth = now.withDayOfMonth(1);

        List<Video> allVideos = new ArrayList<>();

        if (now.getDayOfMonth() <= 2) {
            // Obtener videos del mes pasado
            LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1).withDayOfMonth(1);
            LocalDate endOfLastMonth = startOfCurrentMonth.minusDays(1);
            allVideos.addAll(getVideosForRange(youtubeService, channelId, startOfLastMonth, endOfLastMonth));
        } else if (now.getDayOfMonth() <= 7) {
            // Obtener videos del mes pasado y hasta dos días antes de hoy
            LocalDate startOfLastMonth = startOfCurrentMonth.minusMonths(1).withDayOfMonth(1);
            LocalDate endOfLastMonth = startOfCurrentMonth.minusDays(1);
            allVideos.addAll(getVideosForRange(youtubeService, channelId, startOfLastMonth, endOfLastMonth));

            LocalDate endDate = now.minusDays(2);
            allVideos.addAll(getVideosForRange(youtubeService, channelId, startOfCurrentMonth, endDate));
        } else {
            // Obtener videos del mes actual hasta dos días antes de hoy
            LocalDate endDate = now.minusDays(2);
            allVideos.addAll(getVideosForRange(youtubeService, channelId, startOfCurrentMonth, endDate));
        }

        return allVideos;
    }

    private List<Video> getVideosForRange(YouTube youtubeService, String channelId, LocalDate startDate, LocalDate endDate) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        List<Video> rangeVideos = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            String start = currentDate.format(formatter) + "T00:00:00Z";
            String end = currentDate.format(formatter) + "T23:59:59Z";

            rangeVideos.addAll(fetchVideosFromYouTube(youtubeService, channelId, start, end));
            currentDate = currentDate.plusDays(1);
            // Elimina Thread.sleep(5000);
        }

        return rangeVideos;
    }

    private List<Video> fetchVideosFromYouTube(YouTube youtubeService, String channelId, String startDate, String endDate) throws Exception {
        List<SearchResult> searchResults = new ArrayList<>();
        String nextPageToken = "";

        do {
            com.google.api.services.youtube.YouTube.Search.List searchRequest = youtubeService.search()
                    .list(Collections.singletonList("id,snippet"))
                    .setChannelId(channelId)
                    .setPublishedAfter(startDate)
                    .setPublishedBefore(endDate)
                    .setType(Collections.singletonList("video"))
                    .setMaxResults(50L);

            if (nextPageToken != null && !nextPageToken.isEmpty()) {
                searchRequest.setPageToken(nextPageToken);
            }

            List<SearchResult> currentResults = searchRequest.execute().getItems();
            searchResults.addAll(currentResults);

            nextPageToken = searchRequest.execute().getNextPageToken();
        } while (nextPageToken != null && !nextPageToken.isEmpty());

        List<Video> videos = new ArrayList<>();
        for (SearchResult searchResult : searchResults) {
            String videoId = searchResult.getId().getVideoId();

            VideoListResponse videoResponse = youtubeService.videos()
                    .list(Collections.singletonList("snippet,statistics"))
                    .setId(Collections.singletonList(videoId))
                    .execute();

            if (videoResponse.getItems().isEmpty()) {
                continue;
            }

            com.google.api.services.youtube.model.Video youtubeVideo = videoResponse.getItems().get(0);
            VideoSnippet snippet = youtubeVideo.getSnippet();

            Video video = new Video();
            video.setVideoId(videoId);
            video.setTitle(snippet.getTitle());
            video.setDescription(snippet.getDescription());
            video.setDate(snippet.getPublishedAt().toStringRfc3339());
            video.setViews(youtubeVideo.getStatistics().getViewCount().longValue());
            video.setVisualizaciones(youtubeVideo.getStatistics().getViewCount().longValue());

            BigDecimal revenueBigDecimal = new BigDecimal("0.0");
            Double revenue = revenueBigDecimal.doubleValue();

            video.setEstimatedRevenue(revenue);
            video.setRpm(calculateRpm(revenue, youtubeVideo.getStatistics().getViewCount().longValue()));

            videos.add(video);
        }

        return videos;
    }

    private double calculateRpm(Double revenue, Long views) {
        if (views == 0) {
            return 0.0;
        }
        return (revenue / views) * 1000;
    }
}