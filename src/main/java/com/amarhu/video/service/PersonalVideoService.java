package com.amarhu.video.service;

import com.amarhu.video.dto.VideoDTO;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.video.entity.Video;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonalVideoService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final double REDACTOR_PERCENTAGE = 16.6452;
    private static final BigDecimal MINIMUM_REVENUE = BigDecimal.valueOf(10.0); // Usa el mismo valor que en tu servicio de pagos

    public List<VideoDTO> getVideosForUser(Long userId) {
        // Busca al usuario por ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtén los videos donde la descripción contiene el código del usuario
        List<Video> videos = videoRepository.findByCodigo(user.getCodigo());

        // Mapea los videos al DTO con las ganancias netas calculadas por rol
        return videos.stream()
                .map(video -> {
                    VideoDTO dto = new VideoDTO();
                    dto.setVideoId(video.getVideoId());
                    dto.setTitle(video.getTitle());
                    dto.setDescription(video.getDescription());
                    dto.setDate(video.getDate());
                    dto.setMiniatura(video.getMiniatura());
                    dto.setVisualizaciones(video.getVisualizaciones());

                    double revenueNet = calculateRevenueByRole(user, video);
                    dto.setEstimatedRevenue(revenueNet);

                    // Si quieres también puedes aplicar a AdRevenue (por si lo usas)
                    dto.setEstimatedAdRevenue(video.getEstimatedAdRevenue()); // O puedes aplicar lógica si corresponde

                    dto.setViews(video.getViews());
                    dto.setAverageViewDuration(video.getAverageViewDuration());
                    dto.setRpm(video.getRpm());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private double calculateRevenueByRole(User user, Video video) {
        BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
        BigDecimal personalRevenue = BigDecimal.ZERO;

        switch (user.getRole()) {
            case REDACTOR:
                personalRevenue = revenue.multiply(BigDecimal.valueOf(REDACTOR_PERCENTAGE / 100));
                break;

            case JEFE_REDACCION:
                if (revenue.compareTo(MINIMUM_REVENUE) >= 0) {
                    BigDecimal pagoBase = BigDecimal.ONE;
                    BigDecimal pagoExtra = calculatePagoExtra(revenue);
                    BigDecimal bonoAdicional = calculateBonoAdicional(revenue);
                    personalRevenue = pagoBase
                            .add(pagoExtra)
                            .add(bonoAdicional)
                            .multiply(BigDecimal.valueOf(0.97)); // -3%
                }
                break;

            case JEFE_PRENSA:
                if (revenue.compareTo(MINIMUM_REVENUE) >= 0) {
                    BigDecimal bonoAdicionalJP = calculateBonoAdicionalJP(revenue);
                    personalRevenue = revenue.multiply(BigDecimal.valueOf(0.0468))
                            .add(bonoAdicionalJP)
                            .multiply(BigDecimal.valueOf(0.97));
                }
                break;

            case JEFE_ENTREVISTAS:
                if (revenue.compareTo(MINIMUM_REVENUE) >= 0) {
                    personalRevenue = calculateComisionPro(video.getDescription(), revenue)
                            .multiply(BigDecimal.valueOf(0.97));
                }
                break;

            case PANELISTA:
                if (revenue.compareTo(MINIMUM_REVENUE) >= 0) {
                    personalRevenue = calculateComisionPan(video.getDescription(), revenue)
                            .multiply(BigDecimal.valueOf(0.97));
                }
                break;

            default:
                // Otros roles sin comisión → ganancia 0
                break;
        }

        return personalRevenue.doubleValue();
    }

    // Copias idénticas de tus métodos auxiliares (puedes moverlos a un servicio compartido si quieres):

    private BigDecimal calculatePagoExtra(BigDecimal estimatedRevenue) {
        if (estimatedRevenue.compareTo(BigDecimal.valueOf(20)) <= 0) {
            return BigDecimal.valueOf(1.0);
        } else if (estimatedRevenue.compareTo(BigDecimal.valueOf(25)) <= 0) {
            return BigDecimal.valueOf(1.2);
        } else if (estimatedRevenue.compareTo(BigDecimal.valueOf(30)) <= 0) {
            return BigDecimal.valueOf(1.4);
        } else if (estimatedRevenue.compareTo(BigDecimal.valueOf(35)) <= 0) {
            return BigDecimal.valueOf(1.6);
        } else if (estimatedRevenue.compareTo(BigDecimal.valueOf(40)) <= 0) {
            return BigDecimal.valueOf(1.8);
        } else {
            return BigDecimal.valueOf(2.0);
        }
    }

    private BigDecimal calculateBonoAdicional(BigDecimal estimatedRevenue) {
        if (estimatedRevenue.compareTo(BigDecimal.valueOf(40)) > 0) {
            BigDecimal extraBonos = estimatedRevenue.subtract(BigDecimal.valueOf(40))
                    .divide(BigDecimal.valueOf(40), BigDecimal.ROUND_DOWN)
                    .multiply(BigDecimal.valueOf(1.5));
            return extraBonos.divide(BigDecimal.valueOf(3), BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateBonoAdicionalJP(BigDecimal estimatedRevenue) {
        if (estimatedRevenue.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return estimatedRevenue.divide(BigDecimal.valueOf(40), BigDecimal.ROUND_DOWN)
                    .multiply(BigDecimal.valueOf(1.5));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateComisionPro(String description, BigDecimal revenue) {
        boolean isPro = description.contains("PRO");
        boolean isDir = description.contains("DIR");
        long panCount = description.chars().filter(ch -> ch == 'P').count();

        if (isPro && isDir) {
            return revenue.multiply(BigDecimal.valueOf(0.39));
        } else if (isPro && panCount == 1) {
            return revenue.multiply(BigDecimal.valueOf(0.195));
        } else if (isPro && panCount >= 2) {
            return revenue.multiply(BigDecimal.valueOf(0.156));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateComisionPan(String description, BigDecimal revenue) {
        long panCount = description.chars().filter(ch -> ch == 'P').count();

        if (panCount == 1) {
            return revenue.multiply(BigDecimal.valueOf(0.195));
        } else if (panCount >= 2) {
            return revenue.multiply(BigDecimal.valueOf(0.156));
        }
        return BigDecimal.ZERO;
    }
}
