package com.amarhu.video.service;

import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    // Constructor explícito para inyección de dependencias
    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public List<Video> getVideosForDirectivos() {
        // Obtiene la fecha actual y calcula el rango de fechas
        LocalDate now = LocalDate.now();
        LocalDate startOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        // Busca y devuelve los videos entre el mes actual y el mes anterior
        return videoRepository.findByDateBetween(startOfPreviousMonth.toString(), now.toString());
    }


}
