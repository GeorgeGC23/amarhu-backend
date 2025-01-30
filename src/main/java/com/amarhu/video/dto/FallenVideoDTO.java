package com.amarhu.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FallenVideoDTO {
    private String id;
    private String title;
    private String description;
    private String date; // Fecha formateada
    private long views;
    private double estimatedRevenue;
    private double estimatedAdRevenue;
    private long videoViews;
    private Long averageViewDuration;
    private double rpm;
    private String thumbnail;
}
