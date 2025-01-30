package com.amarhu.video.controller;

import com.amarhu.video.entity.Video;
import com.amarhu.video.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    // Endpoint exclusivo para directivos
    @GetMapping
    public List<Video> getVideosForDirectivos() {
        return videoService.getVideosForDirectivos();
    }
}
