package com.amarhu.production.entity;

import com.amarhu.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Production {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private int totalVideos;

    private int videosCaidos;

    @Column(nullable = false)
    private BigDecimal gananciaTotal;

    @Column(nullable = false)
    private BigDecimal gananciaMenosImpuestos;

    @Column(nullable = false)
    private BigDecimal gananciaNeta;

    private int costeProduccion;

    @Column(nullable = false)
    private BigDecimal costeTotalProduccion;

    @Column(nullable = false)
    private BigDecimal totalGeneradoPorCaidos;

    @Column(nullable = false)
    private LocalDate date; // Fecha de la producción
}
