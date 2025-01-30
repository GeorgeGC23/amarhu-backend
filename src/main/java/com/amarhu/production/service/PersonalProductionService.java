package com.amarhu.production.service;

import com.amarhu.production.dto.PersonalProductionDTO;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.video.entity.Video;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PersonalProductionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final BigDecimal REDACTOR_PERCENTAGE = BigDecimal.valueOf(16.6452);
    private static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(3.72); // Ejemplo de tipo de cambio
    private static final BigDecimal MINIMUM_REVENUE = BigDecimal.valueOf(10.0); // Límite para videos "caídos"

    public PersonalProductionDTO getPersonalProduction(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != Role.REDACTOR) {
            throw new RuntimeException("El rol aún no tiene fórmula implementada.");
        }

        // Buscar videos asociados al usuario según el código en la descripción
        List<Video> videos = videoRepository.findByDescriptionContaining(user.getCodigo());

        int videosTotales = videos.size();
        int videosCaidos = 0;

        // Calcular la ganancia total excluyendo videos "caídos"
        BigDecimal gananciaTotal = BigDecimal.ZERO;

        for (Video video : videos) {
            BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue().doubleValue());
            if (revenue.compareTo(MINIMUM_REVENUE) < 0) {
                videosCaidos++;
            } else {
                gananciaTotal = gananciaTotal.add(revenue); // Solo sumar videos no caídos
            }
        }

        // Calcular la comisión únicamente sobre videos no caídos
        BigDecimal comisionDolares = gananciaTotal.multiply(REDACTOR_PERCENTAGE)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal comisionSoles = comisionDolares.multiply(EXCHANGE_RATE);

        // Crear y devolver el DTO
        return new PersonalProductionDTO(
                userId.toString(),
                userId,
                videosTotales,
                videosCaidos,
                gananciaTotal,
                comisionDolares,
                comisionSoles
        );
    }
}
