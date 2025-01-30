package com.amarhu.video.service;

import com.amarhu.video.dto.FallenVideoDTO;
import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class FallenVideoService {

    @Autowired
    private VideoRepository videoRepository;

    private static final double REVENUE_THRESHOLD = 10.0;

    public List<FallenVideoDTO> getFallenVideos() {
        // Obtener todos los videos desde el repositorio
        List<Video> videos = videoRepository.findAll();

        // Filtrar videos caídos y mapear al DTO
        return videos.stream()
                .filter(video -> video.getEstimatedRevenue() < REVENUE_THRESHOLD) // Filtro de videos caídos
                .map(video -> {
                    String formattedDate = formatDate(video.getDate());
                    return new FallenVideoDTO(
                            video.getVideoId(),
                            video.getTitle(),
                            video.getDescription(),
                            formattedDate,
                            video.getViews(),
                            video.getEstimatedRevenue(),
                            video.getEstimatedAdRevenue(),
                            video.getViews(),
                            video.getAverageViewDuration(),
                            video.getRpm(),
                            video.getMiniatura()
                    );
                })
                .collect(Collectors.toList());
    }

    private String formatDate(String rawDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd - MMMM - yyyy", Locale.ENGLISH);
            return outputFormat.format(inputFormat.parse(rawDate));
        } catch (Exception e) {
            throw new RuntimeException("Error formateando la fecha: " + rawDate, e);
        }
    }
}
