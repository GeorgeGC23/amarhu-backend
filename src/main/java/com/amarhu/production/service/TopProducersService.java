package com.amarhu.production.service;

import com.amarhu.production.dto.TopProducerDTO;
import com.amarhu.production.entity.Production;
import com.amarhu.production.repository.ProductionRepository;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopProducersService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ProductionRepository productionRepository;

    private static final BigDecimal REVENUE_THRESHOLD = BigDecimal.valueOf(10); // Umbral para videos caídos
    private static final BigDecimal TAX_PERCENTAGE = BigDecimal.valueOf(0.78); // Porcentaje de impuestos
    private static final BigDecimal PRODUCTION_COST = BigDecimal.valueOf(5.4); // Coste por video

    public List<TopProducerDTO> getTopProducers() {
        // Obtener todos los usuarios con rol de REDACTOR
        List<User> redactores = userRepository.findByRole(Role.REDACTOR);

        return redactores.stream().map(redactor -> {
                    // Obtener videos asociados al redactor por su código
                    List<Video> videos = videoRepository.findByDescriptionContaining(redactor.getCodigo());

                    // Calcular producción del redactor
                    int totalVideos = videos.size();
                    int videosCaidos = (int) videos.stream()
                            .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(REVENUE_THRESHOLD) < 0)
                            .count();

                    BigDecimal gananciaTotal = videos.stream()
                            .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal gananciaNeta = gananciaTotal.multiply(TAX_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);

                    BigDecimal costeTotalProduccion = BigDecimal.valueOf(totalVideos).multiply(PRODUCTION_COST);

                    // Retornar el DTO con la información calculada
                    return new TopProducerDTO(
                            redactor.getId(),
                            redactor.getName(),
                            totalVideos,
                            videosCaidos,
                            gananciaTotal,
                            gananciaNeta,
                            costeTotalProduccion,
                            redactor.getCodigo()
                    );
                })
                .sorted((p1, p2) -> Integer.compare(p2.getTotalVideos(), p1.getTotalVideos())) // Ordenar por totalVideos descendente
                .collect(Collectors.toList());
    }
}
