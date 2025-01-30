package com.amarhu.news.dto;

import lombok.Data;

@Data
public class NewsDTO {
    private Long id;
    private String title;
    private String content;
    private String image;
    private String date;
}