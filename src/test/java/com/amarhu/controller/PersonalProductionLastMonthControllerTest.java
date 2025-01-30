package com.amarhu.controller;

import com.amarhu.production.dto.PersonalProductionDTO;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonalProductionLastMonthControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetPersonalProductionLastMonthEndpoint() {
        // Recuperar el usuario RA2005
        User user = userRepository.findByCodigo("RA2005")
                .orElseThrow(() -> new RuntimeException("Usuario RA2005 no encontrado"));

        // Construir la URL del endpoint
        String url = "/api/personal-production/last-month/" + user.getId();

        // Ejecutar la solicitud GET
        ResponseEntity<PersonalProductionDTO> response = restTemplate.getForEntity(url, PersonalProductionDTO.class);

        // Validar la respuesta
        assertEquals(200, response.getStatusCodeValue(), "El código de estado debe ser 200");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("RA2005", user.getCodigo(), "El código del usuario no coincide");
    }
}
