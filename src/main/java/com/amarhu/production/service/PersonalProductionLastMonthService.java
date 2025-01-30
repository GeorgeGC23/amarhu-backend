package com.amarhu.production.service;

import com.amarhu.production.dto.PersonalProductionDTO;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonalProductionLastMonthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final BigDecimal REDACTOR_PERCENTAGE = BigDecimal.valueOf(16.6452);
    private static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(3.72); // Tipo de cambio
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PersonalProductionDTO getPersonalProductionLastMonth(Long userId) {
        // Obtener usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getRole() != Role.REDACTOR) {
            throw new RuntimeException("El rol aún no tiene fórmula implementada.");
        }

        // Obtener el mes pasado
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String startDate = lastMonth.atDay(1).format(DATE_FORMATTER);
        String endDate = lastMonth.atEndOfMonth().format(DATE_FORMATTER);

        // Filtrar videos por código y rango de fechas
        List<Video> videos = videoRepository.findAll().stream()
                .filter(video -> isWithinDateRange(video.getDate(), startDate, endDate)
                        && video.getDescription().contains(user.getCodigo()))
                .collect(Collectors.toList());

        // Calcular métricas
        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(BigDecimal.valueOf(10)) < 0)
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionDolares = gananciaTotal.multiply(REDACTOR_PERCENTAGE).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal comisionSoles = comisionDolares.multiply(EXCHANGE_RATE);

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

    private boolean isWithinDateRange(String rawDate, String startDate, String endDate) {
        try {
            // Convertir fecha de video al formato LocalDate
            LocalDate videoDate = LocalDate.parse(rawDate.substring(0, 10), DATE_FORMATTER);
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

            // Validar rango de fechas
            return (videoDate.isEqual(start) || videoDate.isAfter(start))
                    && (videoDate.isEqual(end) || videoDate.isBefore(end));
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la fecha: " + rawDate, e);
        }
    }
}
