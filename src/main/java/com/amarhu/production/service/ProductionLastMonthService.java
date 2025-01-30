package com.amarhu.production.service;

import com.amarhu.production.entity.Production;
import com.amarhu.production.repository.ProductionRepository;
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
public class ProductionLastMonthService {

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final BigDecimal REVENUE_THRESHOLD = BigDecimal.valueOf(10); // Umbral para videos caídos
    private static final BigDecimal TAX_PERCENTAGE = BigDecimal.valueOf(0.78); // Porcentaje de impuestos
    private static final BigDecimal PRODUCTION_COST = BigDecimal.valueOf(5.4); // Coste por video
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Formato de fecha

    public Production getTotalProductionForLastMonth() {
        // Obtener el rango de fechas del mes pasado
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String startDate = lastMonth.atDay(1).format(DATE_FORMATTER);
        String endDate = lastMonth.atEndOfMonth().format(DATE_FORMATTER);

        // Filtrar videos del mes pasado
        List<Video> videos = videoRepository.findByDateBetween(startDate, endDate).stream()
                .filter(video -> isValidDateRange(video.getDate(), startDate, endDate))
                .collect(Collectors.toList());

        // Calcular la producción total
        return calculateTotalProduction(videos, lastMonth.atEndOfMonth());
    }

    private Production calculateTotalProduction(List<Video> videos, LocalDate productionDate) {
        int totalVideos = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(REVENUE_THRESHOLD) < 0)
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gananciaMenosImpuestos = gananciaTotal.multiply(TAX_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalGeneradoPorCaidos = videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(REVENUE_THRESHOLD) < 0)
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal costeTotalProduccion = BigDecimal.valueOf(totalVideos).multiply(PRODUCTION_COST);

        BigDecimal gananciaNeta = gananciaMenosImpuestos.subtract(totalGeneradoPorCaidos);

        // Crear la entidad Production
        Production production = new Production();
        production.setUser(null); // No hay un usuario específico
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

    private boolean isValidDateRange(String rawDate, String startDate, String endDate) {
        try {
            LocalDate videoDate = LocalDate.parse(rawDate, DATE_FORMATTER);
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            return (videoDate.isEqual(start) || videoDate.isAfter(start)) &&
                    (videoDate.isEqual(end) || videoDate.isBefore(end));
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la fecha: " + rawDate, e);
        }
    }
}
