package com.amarhu.news.service;

import com.amarhu.news.dto.NewsDTO;
import com.amarhu.news.entity.News;
import com.amarhu.news.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    public List<NewsDTO> getAllNews() {
        List<News> noticias = newsRepository.findAll();
        return noticias.stream().map(news -> {
            NewsDTO dto = new NewsDTO();
            dto.setId(news.getId());
            dto.setTitle(news.getTitle());
            dto.setContent(news.getContent());
            dto.setImage(news.getImage());
            dto.setDate(news.getDate().toString());
            return dto;
        }).collect(Collectors.toList());
    }
}