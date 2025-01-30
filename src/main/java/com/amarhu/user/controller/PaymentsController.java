package com.amarhu.user.controller;

import com.amarhu.user.dto.PaymentDTO;
import com.amarhu.user.service.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller: PaymentsController.java
@RestController
@RequestMapping("/api/pagos")
public class PaymentsController {

    @Autowired
    private PaymentsService paymentsService;

    @GetMapping
    public List<PaymentDTO> getAllPayments() {
        return paymentsService.getAllPayments();
    }
}
