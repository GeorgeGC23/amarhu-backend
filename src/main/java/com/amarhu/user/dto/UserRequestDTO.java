package com.amarhu.user.dto;

import com.amarhu.user.entity.Role;

public class UserRequestDTO {
    private String name;
    private String email;
    private Role requestedRole;

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRequestedRole() {
        return requestedRole;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRequestedRole(Role requestedRole) {
        this.requestedRole = requestedRole;
    }
}
