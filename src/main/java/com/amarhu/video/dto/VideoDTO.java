package com.amarhu.video.dto;

import lombok.Data;

@Data
public class VideoDTO {

    private String videoId;
    private Integer number;
    private String title;
    private String description;
    private String date;
    private Long visualizaciones;
    private Double estimatedRevenue;
    private Double estimatedAdRevenue;
    private Long views;
    private Long averageViewDuration;
    private Double rpm;
    private String miniatura;
}
