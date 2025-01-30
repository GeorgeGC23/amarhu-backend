package com.amarhu.controller;

import com.amarhu.video.controller.PersonalVideoController;
import com.amarhu.video.dto.VideoDTO;
import com.amarhu.video.service.PersonalVideoService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



public class PersonalVideoControllerTest {

    @Mock
    private PersonalVideoService personalVideoService;

    @InjectMocks
    private PersonalVideoController personalVideoController;

    private MockMvc mockMvc;

    public PersonalVideoControllerTest() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(personalVideoController).build();
    }

    @Test
    public void testGetVideosForRedactor_Success() throws Exception {
        VideoDTO video1 = new VideoDTO();
        video1.setVideoId("v1");
        video1.setTitle("Video Test 1");
        video1.setVisualizaciones(100L);
        video1.setEstimatedRevenue(50.0);

        when(personalVideoService.getVideosForRedactor(1L)).thenReturn(List.of(video1));

        mockMvc.perform(get("/api/personal-videos/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].videoId").value("v1"))
                .andExpect(jsonPath("$[0].title").value("Video Test 1"))
                .andExpect(jsonPath("$[0].visualizaciones").value(100))
                .andExpect(jsonPath("$[0].estimatedRevenue").value(50.0));
    }
}
