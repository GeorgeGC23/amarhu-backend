package com.amarhu.user.dto;

public class AuthResponse {
    private String jwt;
    private UserDTO user;

    // Constructor vacío
    public AuthResponse() {}

    // Constructor con JWT solo (por si acaso)
    public AuthResponse(String jwt) {
        this.jwt = jwt;
    }

    // Constructor completo
    public AuthResponse(String jwt, UserDTO user) {
        this.jwt = jwt;
        this.user = user;
    }

    // Getters y Setters
    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
