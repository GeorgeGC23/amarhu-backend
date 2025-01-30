package com.amarhu.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopProducerDTO {
    private Long userId;
    private String name;
    private int totalVideos;
    private int videosCaidos;
    private BigDecimal gananciaTotal;
    private BigDecimal gananciaNeta;
    private BigDecimal costeTotalProduccion;
    private String codigo;
}
