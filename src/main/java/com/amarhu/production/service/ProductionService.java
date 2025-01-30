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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionService {

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final BigDecimal REVENUE_THRESHOLD = BigDecimal.valueOf(10); // Umbral para videos caídos
    private static final BigDecimal TAX_PERCENTAGE = BigDecimal.valueOf(0.78); // Porcentaje de impuestos
    private static final BigDecimal PRODUCTION_COST = BigDecimal.valueOf(5.4); // Coste por video

    public Production calculateMonthlyProduction() {
        // Obtener el rango de fechas del mes actual
        YearMonth currentMonth = YearMonth.now();
        String startDate = currentMonth.atDay(1).toString(); // Primer día del mes
        String endDate = currentMonth.atEndOfMonth().toString(); // Último día del mes

        // Obtener todos los videos del mes actual
        List<Video> videos = videoRepository.findByDateBetween(startDate, endDate).stream()
                .collect(Collectors.toList());

        return calculateTotalProduction(videos, LocalDate.now());
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

        // Crear entidad Production
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
        production.setDate(productionDate); // Fecha actual

        return productionRepository.save(production);
    }
}
