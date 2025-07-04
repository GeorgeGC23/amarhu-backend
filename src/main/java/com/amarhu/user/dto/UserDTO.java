package com.amarhu.user.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String codigo;
    private String avatar;
    private LocalDateTime createdAt;
    private boolean active;

    // Constructor vacío
    public UserDTO() {}

    // Constructor completo
    public UserDTO(Long id, String name, String email, String role, String codigo, String avatar, LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.codigo = codigo;
        this.avatar = avatar;
        this.createdAt = createdAt;
        this.active = active;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}