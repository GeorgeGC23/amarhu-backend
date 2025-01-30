package com.amarhu.production.controller;

import com.amarhu.production.dto.RankingJRDTO;
import com.amarhu.production.service.RankingJRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankingJRs")
public class RankingJRController {

    @Autowired
    private RankingJRService rankingJRService;

    @GetMapping
    public List<RankingJRDTO> getRankingJRs() {
        return rankingJRService.getRankingJRs();
    }
}
