package com.amarhu.user.controller;

import com.amarhu.user.dto.UserRequestDTO;
import com.amarhu.user.service.UserRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-requests")
public class UserRequestController {

    @Autowired
    private UserRequestService userRequestService;

    @PostMapping
    public ResponseEntity<String> createUserRequest(@RequestBody UserRequestDTO userRequestDTO) {
        userRequestService.createUserRequest(userRequestDTO);
        return ResponseEntity.ok("Solicitud de usuario creada exitosamente.");
    }

    @GetMapping("/{id}/approve")
    public String approveUserRequest(@PathVariable Long id, @RequestParam String token) {
        userRequestService.approveRequest(id, token);
        return "redirect:/success-page.html"; // Redirigir a una página de éxito
    }


}
