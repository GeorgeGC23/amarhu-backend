package com.amarhu.video.service;

import com.amarhu.video.dto.VideoDTO;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.video.entity.Video;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonalVideoService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final double REDACTOR_PERCENTAGE = 16.6452;

    public List<VideoDTO> getVideosForRedactor(Long userId) {
        // Busca al usuario por ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verifica que sea un REDACTOR
        if (user.getRole() != Role.REDACTOR) {
            throw new RuntimeException("El usuario no tiene el rol de REDACTOR");
        }

        // Obtén los videos donde la descripción contiene el código del usuario
        List<Video> videos = videoRepository.findByCodigo(user.getCodigo());

        // Mapea los videos al DTO con las ganancias calculadas
        return videos.stream()
                .map(video -> {
                    VideoDTO dto = new VideoDTO();
                    dto.setVideoId(video.getVideoId());
                    dto.setTitle(video.getTitle());
                    dto.setDescription(video.getDescription());
                    dto.setDate(video.getDate());
                    dto.setMiniatura(video.getMiniatura());
                    dto.setVisualizaciones(video.getVisualizaciones());
                    dto.setEstimatedRevenue(video.getEstimatedRevenue() * (REDACTOR_PERCENTAGE / 100));
                    dto.setEstimatedAdRevenue(video.getEstimatedAdRevenue() * (REDACTOR_PERCENTAGE / 100));
                    dto.setViews(video.getViews());
                    dto.setAverageViewDuration(video.getAverageViewDuration());
                    dto.setRpm(video.getRpm());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
