package com.amarhu.video.controller;

import com.amarhu.video.entity.Video;
import com.amarhu.video.service.YouTubeAnalyticsService;
import com.amarhu.video.service.YouTubeDataService;
import com.amarhu.video.service.YouTubeVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeDataController {

    @Autowired
    private YouTubeDataService youTubeDataService;

    @GetMapping("/process")
    public ResponseEntity<String> processYouTubeData() {
        try {
            youTubeDataService.processYouTubeData();
            return ResponseEntity.ok("YouTube data processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

