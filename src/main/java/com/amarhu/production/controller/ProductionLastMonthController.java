package com.amarhu.production.controller;

import com.amarhu.production.entity.Production;
import com.amarhu.production.service.ProductionLastMonthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production-last-month")
public class ProductionLastMonthController {

    @Autowired
    private ProductionLastMonthService productionLastMonthService;

    @GetMapping
    public Production getProductionForLastMonth() {
        return productionLastMonthService.getTotalProductionForLastMonth();
    }
}
