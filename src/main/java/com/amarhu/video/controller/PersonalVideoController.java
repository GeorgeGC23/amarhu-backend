package com.amarhu.video.controller;

import com.amarhu.video.dto.VideoDTO;
import com.amarhu.video.service.PersonalVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal-videos")
public class PersonalVideoController {

    @Autowired
    private PersonalVideoService personalVideoService;

    // Endpoint para obtener los videos del redactor
    @GetMapping("/{userId}")
    public List<VideoDTO> getVideosForRedactor(@PathVariable Long userId) {
        return personalVideoService.getVideosForRedactor(userId);
    }
}
