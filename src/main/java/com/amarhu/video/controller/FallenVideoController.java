package com.amarhu.video.controller;

import com.amarhu.video.dto.FallenVideoDTO;
import com.amarhu.video.service.FallenVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/caidos")
public class FallenVideoController {

    @Autowired
    private FallenVideoService fallenVideoService;

    @GetMapping
    public List<FallenVideoDTO> getFallenVideos() {
        return fallenVideoService.getFallenVideos();
    }
}
