package com.amarhu.controller;

import com.amarhu.user.dto.PaymentDTO;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentsControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testGetAllPayments() {
        // Verificar que al menos existe un usuario en la base de datos
        List<User> users = userRepository.findAll();
        assertFalse(users.isEmpty(), "La base de datos debe contener al menos un usuario para esta prueba");

        // Realizar la solicitud GET al endpoint
        String url = "/api/pagos";
        ResponseEntity<PaymentDTO[]> response = restTemplate.getForEntity(url, PaymentDTO[].class);

        // Validar la respuesta
        assertEquals(200, response.getStatusCodeValue(), "El código de estado debe ser 200");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertTrue(response.getBody().length > 0, "Debe haber al menos un pago en la respuesta");

        // Validar datos específicos del primer resultado
        PaymentDTO firstPayment = response.getBody()[0];
        assertNotNull(firstPayment.getNombre(), "El nombre del primer usuario no debe ser nulo");
        assertTrue(firstPayment.getVideosTotales() >= 0, "El total de videos debe ser mayor o igual a cero");
    }
}
