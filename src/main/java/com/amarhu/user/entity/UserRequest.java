package com.amarhu.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_request")
public class UserRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role requestedRole;

    private boolean approved;

    private LocalDateTime approvedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(unique = true, nullable = false)
    private String token; // Campo para almacenar el token único

    // Getter y Setter de token (si no usas Lombok)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
