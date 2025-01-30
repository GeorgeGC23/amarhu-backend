package com.amarhu.service;

import com.amarhu.production.dto.PersonalProductionDTO;
import com.amarhu.production.service.PersonalProductionLastMonthService;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PersonalProductionLastMonthServiceTest {

    @Autowired
    private PersonalProductionLastMonthService personalProductionLastMonthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Test
    public void testGetPersonalProductionLastMonth() {
        // Recuperar el usuario RA2005
        User user = userRepository.findByCodigo("RA2005")
                .orElseThrow(() -> new RuntimeException("Usuario RA2005 no encontrado"));

        // Verificar que el usuario tiene videos asociados
        List<Video> videos = videoRepository.findByCodigo(user.getCodigo());
        assertNotNull(videos, "La lista de videos no debe ser nula");
        assertFalse(videos.isEmpty(), "La lista de videos no debe estar vacía");

        // Ejecutar el servicio para obtener la producción del mes pasado
        PersonalProductionDTO result = personalProductionLastMonthService.getPersonalProductionLastMonth(user.getId());

        // Verificar los resultados
        assertNotNull(result, "El resultado no debe ser nulo");
        assertEquals("RA2005", user.getCodigo(), "El código del usuario no coincide");
        assertTrue(result.getGananciaTotal().compareTo(BigDecimal.ZERO) >= 0, "La ganancia total debe ser mayor o igual a cero");
        assertTrue(result.getVideosTotales() >= 0, "El total de videos debe ser mayor o igual a cero");
    }
}
