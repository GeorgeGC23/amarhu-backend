package com.amarhu.user.service;

import com.amarhu.user.dto.PaymentDTO;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    private static final BigDecimal REDACTOR_PERCENTAGE = BigDecimal.valueOf(16.6452);
    private static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(3.72); // Tipo de cambio ejemplo
    private static final BigDecimal MINIMUM_REVENUE = BigDecimal.valueOf(10.0); // Límite para videos caídos

    public List<PaymentDTO> getAllPayments() {
        List<User> workers = userRepository.findAll();

        return workers.stream()
                .filter(worker -> worker.getRole() != Role.DIRECTIVO) // Excluir directivos
                .map(worker -> {
                    switch (worker.getRole()) {
                        case REDACTOR:
                            return calculateRedactorPayment(worker);
                        case JEFE_REDACCION:
                            return calculateJefeRedaccionPayment(worker);
                        case JEFE_PRENSA:
                            return calculateJefePrensaPayment(worker);
                        case JEFE_ENTREVISTAS:
                            return calculateJefeEntrevistasPayment(worker);
                        case PANELISTA:
                            return calculatePanelistaPayment(worker);
                        default:
                            throw new RuntimeException("El rol " + worker.getRole() + " aún no tiene fórmula de pago implementada.");
                    }
                })
                .collect(Collectors.toList());
    }

    private PaymentDTO calculateRedactorPayment(User worker) {
        // Obtener los videos asociados al redactor
        List<Video> videos = videoRepository.findByDescriptionContaining(worker.getCodigo());

        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) < 0)
                .count();

        // Calcular ganancias excluyendo los videos caídos
        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gananciaMenosImpuestos = gananciaTotal.multiply(BigDecimal.valueOf(0.78)); // Aplicar impuestos
        BigDecimal gananciaDespuesCaidos = videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular comisión
        BigDecimal comisionDolares = gananciaDespuesCaidos.multiply(REDACTOR_PERCENTAGE)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        BigDecimal comisionSoles = comisionDolares.multiply(EXCHANGE_RATE);

        return new PaymentDTO(
                UUID.randomUUID().toString(),
                worker.getCodigo(),
                worker.getName(),
                videosTotales,
                videosCaidos,
                gananciaTotal,
                gananciaMenosImpuestos,
                gananciaDespuesCaidos,
                comisionDolares,
                comisionSoles
        );
    }

    private PaymentDTO calculateJefeRedaccionPayment(User worker) {
        // Obtener los videos asociados al jefe de redacción
        List<Video> videos = videoRepository.findByDescriptionContaining(worker.getCodigo());

        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) < 0)
                .count();

        // Filtrar videos no caídos y aplicar lógica de cálculo
        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gananciaMenosImpuestos = gananciaTotal.multiply(BigDecimal.valueOf(0.78)); // Aplicar impuestos

        BigDecimal comisionTotal = videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                .map(video -> {
                    BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
                    BigDecimal pagoExtra = calculatePagoExtra(revenue);
                    BigDecimal bonoAdicional = calculateBonoAdicional(revenue);
                    return revenue.multiply(BigDecimal.valueOf(0.97)) // Descuento del 3%
                            .add(pagoExtra)
                            .add(bonoAdicional);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionSoles = comisionTotal.multiply(EXCHANGE_RATE);

        return new PaymentDTO(
                UUID.randomUUID().toString(),
                worker.getCodigo(),
                worker.getName(),
                videosTotales,
                videosCaidos,
                gananciaTotal,
                gananciaMenosImpuestos,
                comisionTotal, // Para JR, la comisión final es igual a la ganancia después de impuestos
                comisionTotal,
                comisionSoles
        );
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
            // Calcular bono adicional en múltiplos de $40
            BigDecimal extraBonos = estimatedRevenue.subtract(BigDecimal.valueOf(40))
                    .divide(BigDecimal.valueOf(40), RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(1.5));
            return extraBonos.divide(BigDecimal.valueOf(3), RoundingMode.HALF_UP); // Un tercio para el jefe de redacción
        }
        return BigDecimal.ZERO;
    }

    private PaymentDTO calculateJefePrensaPayment(User worker) {
        List<Video> videos = videoRepository.findByDescriptionContaining(worker.getCodigo());

        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) < 0)
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gananciaMenosImpuestos = gananciaTotal.multiply(BigDecimal.valueOf(0.78)); // Aplicar impuestos

        BigDecimal comisionTotal = videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                .map(video -> {
                    BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
                    BigDecimal bonoAdicional = calculateBonoAdicionalJP(revenue);
                    return revenue.multiply(BigDecimal.valueOf(0.0468)) // Comisión base
                            .add(bonoAdicional)
                            .multiply(BigDecimal.valueOf(0.97)); // Descuento del 3%
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionSoles = comisionTotal.multiply(EXCHANGE_RATE);

        return new PaymentDTO(
                UUID.randomUUID().toString(),
                worker.getCodigo(),
                worker.getName(),
                videosTotales,
                videosCaidos,
                gananciaTotal,
                gananciaMenosImpuestos,
                comisionTotal,
                comisionTotal,
                comisionSoles
        );
    }

    private PaymentDTO calculateJefeEntrevistasPayment(User worker) {
        List<Video> videos = videoRepository.findByDescriptionContaining(worker.getCodigo());

        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) < 0)
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionTotal = videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                .map(video -> {
                    BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
                    return calculateComisionPro(video.getDescription(), revenue)
                            .multiply(BigDecimal.valueOf(0.97)); // Descuento del 3%
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionSoles = comisionTotal.multiply(EXCHANGE_RATE);

        return new PaymentDTO(
                UUID.randomUUID().toString(),
                worker.getCodigo(),
                worker.getName(),
                videosTotales,
                videosCaidos,
                gananciaTotal,
                gananciaTotal, // Sin impuestos, ya que no se aplicaron en este rol
                comisionTotal,
                comisionTotal,
                comisionSoles
        );
    }

    private PaymentDTO calculatePanelistaPayment(User worker) {
        List<Video> videos = videoRepository.findByDescriptionContaining(worker.getCodigo());

        int videosTotales = videos.size();
        int videosCaidos = (int) videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) < 0)
                .count();

        BigDecimal gananciaTotal = videos.stream()
                .map(video -> BigDecimal.valueOf(video.getEstimatedRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionTotal = videos.stream()
                .filter(video -> BigDecimal.valueOf(video.getEstimatedRevenue()).compareTo(MINIMUM_REVENUE) >= 0)
                .map(video -> {
                    BigDecimal revenue = BigDecimal.valueOf(video.getEstimatedRevenue());
                    return calculateComisionPan(video.getDescription(), revenue)
                            .multiply(BigDecimal.valueOf(0.97)); // Descuento del 3%
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal comisionSoles = comisionTotal.multiply(EXCHANGE_RATE);

        return new PaymentDTO(
                UUID.randomUUID().toString(),
                worker.getCodigo(),
                worker.getName(),
                videosTotales,
                videosCaidos,
                gananciaTotal,
                gananciaTotal, // Sin impuestos, ya que no se aplicaron en este rol
                comisionTotal,
                comisionTotal,
                comisionSoles
        );
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
