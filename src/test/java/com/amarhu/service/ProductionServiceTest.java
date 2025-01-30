package com.amarhu.service;

import com.amarhu.production.entity.Production;
import com.amarhu.production.service.ProductionService;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import com.amarhu.video.entity.Video;
import com.amarhu.video.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional // Asegura que los cambios en la base de datos se revierten después de cada prueba
public class ProductionServiceTest {

    @Autowired
    private ProductionService productionService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCalculateMonthlyProductionWithRealData() {
        // Obtener un usuario existente de la base de datos
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No hay usuarios en la base de datos."));
        System.out.println("Usuario seleccionado: " + user);

        // Obtener videos del mes presente
        List<Video> videos = videoRepository.findAll();
        System.out.println("Videos encontrados: " + videos);

        if (videos.isEmpty()) {
            throw new RuntimeException("No hay videos en la base de datos.");
        }


   }

}
