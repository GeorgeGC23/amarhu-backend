package com.amarhu.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
// DTO: PaymentDTO.java
@Data
@AllArgsConstructor
public class PaymentDTO {
    private String id;
    private String codigo;
    private String nombre;
    private int videosTotales;
    private int videosCaidos;
    private BigDecimal gananciaTotal;
    private BigDecimal gananciaMenosImpuestos;
    private BigDecimal gananciaDespuesCaidos;
    private BigDecimal comisionDolares;
    private BigDecimal comisionSoles;
}

