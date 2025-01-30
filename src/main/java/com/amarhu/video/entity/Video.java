package com.amarhu.video.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.w3c.dom.Text;

@Entity
@Data
public class Video {

    @Id
    private String videoId;

    private Integer number;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private String date;

    private Long visualizaciones;

    private Double estimatedRevenue;

    private Double estimatedAdRevenue;

    private Long views;

    private Long averageViewDuration;

    private Double rpm;

    private String miniatura;

    @Column(nullable = false, updatable = false)
    private String createdAt;


}
