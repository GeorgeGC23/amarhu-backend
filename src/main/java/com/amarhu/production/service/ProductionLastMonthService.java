package com.amarhu.production.service;

import com.amarhu.production.entity.Production;
import com.amarhu.production.repository.ProductionRepository;
import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionLastMonthService {

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final BigDecimal REVENUE_THRESHOLD = BigDecimal.valueOf(10); // Umbral para videos caídos
    private static final BigDecimal TAX_PERCENTAGE = BigDecimal.valueOf(0.78); // Porcentaje de impuestos
    private static final BigDecimal PRODUCTION_COST = BigDecimal.valueOf(5.4); // Coste por video

    // <-- configurable: días de tolerancia (videos con <= GRACE_PERIOD_DAYS no son considerados caídos)
    private static final int GRACE_PERIOD_DAYS = 2;

    public Production getTotalProductionForLastMonth() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String startDate = lastMonth.atDay(1).toString();
        String endDate = lastMonth.atEndOfMonth().toString();

        List<Video> videos = videoRepository.findByDateBetween(startDate, endDate).stream()
                .collect(Collectors.toList());

        return calculateTotalProduction(videos, lastMonth.atEndOfMonth());
    }

    private Production calculateTotalProduction(List<Video> videos, LocalDate productionDate) {
        int totalVideos = videos.size();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        int videosCaidos = (int) videos.stream()
                .filter(video -> isVideoCaido(video, today))
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gananciaMenosImpuestos = gananciaTotal.multiply(TAX_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalGeneradoPorCaidos = videos.stream()
                .filter(video -> isVideoCaido(video, today))
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal costeTotalProduccion = BigDecimal.valueOf(totalVideos).multiply(PRODUCTION_COST);

        BigDecimal gananciaNeta = gananciaMenosImpuestos.subtract(totalGeneradoPorCaidos);

        Production production = new Production();
        production.setUser(null);
        production.setTotalVideos(totalVideos);
        production.setVideosCaidos(videosCaidos);
        production.setGananciaTotal(gananciaTotal);
        production.setGananciaMenosImpuestos(gananciaMenosImpuestos);
        production.setGananciaNeta(gananciaNeta);
        production.setCosteProduccion(PRODUCTION_COST.intValue());
        production.setCosteTotalProduccion(costeTotalProduccion);
        production.setTotalGeneradoPorCaidos(totalGeneradoPorCaidos);
        production.setDate(productionDate);

        return productionRepository.save(production);
    }

    /**
     * Devolver true si el video debe considerarse "caído" para los cálculos.
     * Reglas:
     *  - Si revenue >= REVENUE_THRESHOLD -> no es caído.
     *  - Si revenue < REVENUE_THRESHOLD:
     *      * Si el video fue subido hace <= GRACE_PERIOD_DAYS -> no se considera caído (pendiente).
     *      * Si fue subido hace > GRACE_PERIOD_DAYS -> sí se considera caído.
     *
     * Manejo de fechas: parseamos el String ISO (ej. "2025-09-06T07:00:37.000Z") usando Instant.parse.
     * Si no se puede parsear la fecha, por seguridad retornamos false (no considerarlo caído).
     */
    private boolean isVideoCaido(Video video, LocalDate todayUtc) {
        // revenue seguro
        BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue() == null ? 0.0 : video.getEstimatedRevenue());

        if (revenue.compareTo(REVENUE_THRESHOLD) >= 0) {
            return false;
        }

        String dateStr = video.getDate();
        if (dateStr == null || dateStr.isBlank()) {
            return false;
        }

        try {
            LocalDate fechaVideo = Instant.parse(dateStr).atZone(ZoneOffset.UTC).toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(fechaVideo, todayUtc);
            return daysBetween > GRACE_PERIOD_DAYS;
        } catch (Exception e) {
            return false;
        }
    }
}