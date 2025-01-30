package com.amarhu.news.controller;

import com.amarhu.news.dto.NewsDTO;
import com.amarhu.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping
    public List<NewsDTO> getNews() {
        return newsService.getAllNews();
    }
}