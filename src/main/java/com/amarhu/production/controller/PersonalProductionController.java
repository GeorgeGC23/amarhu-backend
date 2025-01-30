package com.amarhu.production.controller;

import com.amarhu.production.dto.PersonalProductionDTO;
import com.amarhu.production.service.PersonalProductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personal-production")
public class PersonalProductionController {

    @Autowired
    private PersonalProductionService personalProductionService;

    @GetMapping("/{userId}")
    public PersonalProductionDTO getPersonalProduction(@PathVariable Long userId) {
        return personalProductionService.getPersonalProduction(userId);
    }
}
