package com.amarhu.production.controller;

import com.amarhu.production.dto.PersonalProductionDTO;
import com.amarhu.production.service.PersonalProductionLastMonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personal-production")
public class PersonalProductionLastMonthController {

    @Autowired
    private PersonalProductionLastMonthService personalProductionLastMonthService;

    @GetMapping("/last-month/{userId}")
    public PersonalProductionDTO getPersonalProductionLastMonth(@PathVariable Long userId) {
        return personalProductionLastMonthService.getPersonalProductionLastMonth(userId);
    }
}
