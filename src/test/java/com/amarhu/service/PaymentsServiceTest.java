package com.amarhu.service;

import com.amarhu.user.dto.PaymentDTO;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.user.service.PaymentsService;
import com.amarhu.video.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PaymentsServiceTest {

    @Autowired
    private PaymentsService paymentsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Test
    public void testGetAllPayments() {
        // Verificar que la base de datos contiene usuarios y videos
        assertFalse(userRepository.findAll().isEmpty(), "Debe haber usuarios en la base de datos");
        assertFalse(videoRepository.findAll().isEmpty(), "Debe haber videos en la base de datos");

        // Ejecutar el servicio
        List<PaymentDTO> payments = paymentsService.getAllPayments();

        // Validar que se generaron pagos
        assertNotNull(payments, "La lista de pagos no debe ser nula");
        assertFalse(payments.isEmpty(), "Debe generarse al menos un pago");

        // Validar datos de un pago específico (ejemplo: primer pago)
        PaymentDTO firstPayment = payments.get(0);
        assertNotNull(firstPayment.getNombre(), "El nombre del primer usuario no debe ser nulo");
        assertTrue(firstPayment.getVideosTotales() >= 0, "El total de videos debe ser mayor o igual a cero");
        assertTrue(firstPayment.getGananciaTotal().compareTo(BigDecimal.ZERO) >= 0, "La ganancia total debe ser mayor o igual a cero");
    }
}
