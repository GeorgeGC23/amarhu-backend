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
public class PersonalProductionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final BigDecimal REDACTOR_PERCENTAGE = BigDecimal.valueOf(16.6452);
    private static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(3.62);
    private static final BigDecimal MINIMUM_REVENUE = BigDecimal.valueOf(10.0);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PersonalProductionDTO getPersonalProduction(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        switch (user.getRole()) {
            case REDACTOR:
            case LOCUTOR:
            case EDITOR:
            case PANELISTA:
                return calcularProduccionIndividual(user);

            case JEFE_REDACCION:
            case JEFE_ENTREVISTAS:
            case JEFE_PRENSA:
                return calcularProduccionJefe(user);

            default:
                throw new RuntimeException("El rol aún no tiene fórmula implementada.");
        }
    }

    private PersonalProductionDTO calcularProduccionIndividual(User user) {
        // Mes actual
        YearMonth currentMonth = YearMonth.now();
        String startDate = currentMonth.atDay(1).format(DATE_FORMATTER);
        String endDate = currentMonth.atEndOfMonth().format(DATE_FORMATTER);

        List<Video> videos = videoRepository.findAll().stream()
                .filter(video -> isWithinDateRange(video.getDate(), startDate, endDate)
                        && video.getDescription().contains(user.getCodigo()))
                .collect(Collectors.toList());

        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) < 0)
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionDolares = gananciaTotal
                .multiply(REDACTOR_PERCENTAGE)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

        return new PersonalProductionDTO(
                user.getId().toString(),
                user.getId(),
                videosTotales,
                videosCaidos,
                gananciaTotal,
                comisionDolares,
                comisionDolares.multiply(EXCHANGE_RATE)
        );
    }

    private PersonalProductionDTO calcularProduccionJefe(User user) {
        YearMonth currentMonth = YearMonth.now();
        String startDate = currentMonth.atDay(1).format(DATE_FORMATTER);
        String endDate = currentMonth.atEndOfMonth().format(DATE_FORMATTER);

        List<Video> videos = videoRepository.findAll().stream()
                .filter(video -> isWithinDateRange(video.getDate(), startDate, endDate)
                        && video.getDescription().contains(user.getCodigo()))
                .collect(Collectors.toList());

        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) < 0)
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionDolares = calculateCommissionByRole(user, videos);

        return new PersonalProductionDTO(
                user.getId().toString(),
                user.getId(),
                videosTotales,
                videosCaidos,
                gananciaTotal,
                comisionDolares,
                comisionDolares.multiply(EXCHANGE_RATE)
        );
    }

    private boolean isWithinDateRange(String rawDate, String startDate, String endDate) {
        try {
            LocalDate videoDate = LocalDate.parse(rawDate.substring(0, 10), DATE_FORMATTER);
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);

            return (videoDate.isEqual(start) || videoDate.isAfter(start))
                    && (videoDate.isEqual(end) || videoDate.isBefore(end));
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la fecha: " + rawDate, e);
        }
    }

    private BigDecimal calculateCommissionByRole(User user, List<Video> videos) {
        Role role = user.getRole();
        BigDecimal total = BigDecimal.ZERO;

        switch (role) {
            case JEFE_REDACCION:
                total = videos.stream()
                        .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                        .map(video -> {
                            BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
                            BigDecimal pagoBase = BigDecimal.ONE;
                            BigDecimal pagoExtra = calculatePagoExtra(revenue);
                            BigDecimal bonoAdicional = calculateBonoAdicional(revenue);
                            return pagoBase.add(pagoExtra).add(bonoAdicional)
                                    .multiply(BigDecimal.valueOf(0.97));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                break;

            case JEFE_PRENSA:
                total = videos.stream()
                        .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                        .map(video -> {
                            BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
                            BigDecimal bonoAdicionalJP = calculateBonoAdicionalJP(revenue);
                            return revenue.multiply(BigDecimal.valueOf(0.0468))
                                    .add(bonoAdicionalJP)
                                    .multiply(BigDecimal.valueOf(0.97));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                break;

            case JEFE_ENTREVISTAS:
                total = videos.stream()
                        .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                        .map(video -> {
                            BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
                            return calculateComisionPro(video.getDescription(), revenue)
                                    .multiply(BigDecimal.valueOf(0.97));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                break;

            default:
                throw new RuntimeException("El rol aún no tiene fórmula implementada.");
        }

        return total;
    }

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
                    .divide(BigDecimal.valueOf(40), RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(1.5));
            return extraBonos.divide(BigDecimal.valueOf(3), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateBonoAdicionalJP(BigDecimal estimatedRevenue) {
        if (estimatedRevenue.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return estimatedRevenue.divide(BigDecimal.valueOf(40), RoundingMode.DOWN)
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
}
