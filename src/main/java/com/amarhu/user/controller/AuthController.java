package com.amarhu.user.controller;

import com.amarhu.user.dto.AuthRequest;
import com.amarhu.user.dto.AuthResponse;
import com.amarhu.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        System.out.println("Email recibido: " + request.getEmail());
        System.out.println("Password recibido: " + request.getPassword());

        AuthResponse response = authService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }



}
