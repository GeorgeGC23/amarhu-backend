package com.amarhu.controller;

import com.amarhu.production.entity.Production;
import com.amarhu.production.controller.ProductionLastMonthController;
import com.amarhu.production.service.ProductionLastMonthService;
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

public class ProductionLastMonthControllerTest {

    @Mock
    private ProductionLastMonthService productionLastMonthService;

    @InjectMocks
    private ProductionLastMonthController productionLastMonthController;

    private MockMvc mockMvc;

    public ProductionLastMonthControllerTest() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(productionLastMonthController).build();
    }

    @Test
    public void testGetProductionForLastMonth() throws Exception {
        // Simular datos de producción
        Production production1 = new Production();
        production1.setId(1L);
        production1.setTotalVideos(10);
        production1.setGananciaTotal(new BigDecimal("100.00"));
        production1.setDate(LocalDate.now().minusMonths(1).withDayOfMonth(5));

        Production production2 = new Production();
        production2.setId(2L);
        production2.setTotalVideos(20);
        production2.setGananciaTotal(new BigDecimal("200.00"));
        production2.setDate(LocalDate.now().minusMonths(1).withDayOfMonth(15));

        List<Production> mockProductions = List.of(production1, production2);


        // Probar el endpoint
        mockMvc.perform(get("/api/production-last-month")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].totalVideos").value(10))
                .andExpect(jsonPath("$[0].gananciaTotal").value(100.00))
                .andExpect(jsonPath("$[1].totalVideos").value(20))
                .andExpect(jsonPath("$[1].gananciaTotal").value(200.00));
    }
}
