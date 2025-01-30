package com.amarhu.production.controller;

import com.amarhu.production.dto.TopProducerDTO;
import com.amarhu.production.service.TopProducersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/top-producers")
public class TopProducersController {

    @Autowired
    private TopProducersService topProducersService;

    @GetMapping
    public List<TopProducerDTO> getTopProducers() {
        System.out.println("Fetching top producers...");
        List<TopProducerDTO> topProducers = topProducersService.getTopProducers();
        System.out.println("Fetched " + topProducers.size() + " producers.");
        return topProducers;
    }
}
