package com.amarhu.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PersonalProductionDTO {
    private String id;
    private Long userId;
    private int videosTotales;
    private int videosCaidos;
    private BigDecimal gananciaTotal;
    private BigDecimal comisionDolares;
    private BigDecimal comisionSoles;
}
