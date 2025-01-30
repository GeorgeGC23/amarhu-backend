package com.amarhu.production.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RankingJRDTO {

    private Long userId;
    private String nombre;
    private int totalVideos;
    private int videosCaidos;
    private BigDecimal gananciaTotal;
    private BigDecimal gananciaNeta;
    private BigDecimal costeTotalProduccion;
    private BigDecimal totalGeneradoPorCaidos;

    // Constructor con todos los datos necesarios
    public RankingJRDTO(Long userId, String nombre, int totalVideos, int videosCaidos,
                        BigDecimal gananciaTotal, BigDecimal gananciaNeta,
                        BigDecimal costeTotalProduccion, BigDecimal totalGeneradoPorCaidos) {
        this.userId = userId;
        this.nombre = nombre;
        this.totalVideos = totalVideos;
        this.videosCaidos = videosCaidos;
        this.gananciaTotal = gananciaTotal;
        this.gananciaNeta = gananciaNeta;
        this.costeTotalProduccion = costeTotalProduccion;
        this.totalGeneradoPorCaidos = totalGeneradoPorCaidos;
    }
}
