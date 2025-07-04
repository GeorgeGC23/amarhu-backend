package com.amarhu.video.service;

import com.amarhu.video.dto.FallenVideoDTO;
import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FallenVideoService {

    private static final double REVENUE_THRESHOLD = 10.0;

    private final VideoRepository videoRepository;

    @Autowired
    public FallenVideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public List<FallenVideoDTO> getFallenVideos() {
        // Obtiene la fecha actual y calcula el rango de fechas (mismo que VideoService)
        LocalDate now = LocalDate.now();
        LocalDate startOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        // Busca los videos en el mismo rango que VideoService
        List<Video> videos = videoRepository.findByDateBetween(startOfPreviousMonth.toString(), now.toString());

        // Filtra los videos con ingresos por debajo del umbral y mapea al DTO
        return videos.stream()
                .filter(video -> video.getEstimatedRevenue() < REVENUE_THRESHOLD)
                .map(video -> new FallenVideoDTO(
                        video.getVideoId(),
                        video.getTitle(),
                        video.getDescription(),
                        video.getDate(), // No formateamos la fecha, la dejamos tal cual
                        video.getViews(),
                        video.getEstimatedRevenue(),
                        // Verificamos si getEstimatedAdRevenue() es null antes de usarlo
                        video.getEstimatedAdRevenue() != null ? video.getEstimatedAdRevenue() : 0.0,  // Si es null, asignamos 0.0
                        video.getViews(),
                        video.getAverageViewDuration(),
                        video.getRpm(),
                        video.getMiniatura()
                ))
                .collect(Collectors.toList());
    }
}
