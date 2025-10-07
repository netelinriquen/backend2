package com.inkflow.api.dto;

public class AuthResponse {
    private String token;
    private UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}

class UserResponse {
    private Long id;
    private String nome;
    private String email;
    private Boolean isAdmin;

    public UserResponse(Long id, String nome, String email, Boolean isAdmin) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Boolean getIsAdmin() { return isAdmin; }
}