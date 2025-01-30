package com.amarhu.controller;

import com.amarhu.production.entity.Production;
import com.amarhu.production.controller.ProductionController;
import com.amarhu.production.service.ProductionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductionControllerTest {

    @Mock
    private ProductionService productionService;

    @InjectMocks
    private ProductionController productionController;

    private MockMvc mockMvc;

    public ProductionControllerTest() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(productionController).build();
    }

    @Test
    public void testGetAllProductions() throws Exception {
        // Datos simulados
        Production production1 = new Production();
        production1.setId(1L);
        production1.setTotalVideos(10);
        production1.setGananciaTotal(new BigDecimal("100.00"));
        production1.setDate(LocalDate.of(2023, 1, 1));

        Production production2 = new Production();
        production2.setId(2L);
        production2.setTotalVideos(20);
        production2.setGananciaTotal(new BigDecimal("200.00"));
        production2.setDate(LocalDate.of(2023, 2, 1));

        List<Production> mockProductions = List.of(production1, production2);


        // Prueba del endpoint
        mockMvc.perform(get("/api/production")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].totalVideos").value(10))
                .andExpect(jsonPath("$[0].gananciaTotal").value(100.00))
                .andExpect(jsonPath("$[1].totalVideos").value(20))
                .andExpect(jsonPath("$[1].gananciaTotal").value(200.00));
    }
}
