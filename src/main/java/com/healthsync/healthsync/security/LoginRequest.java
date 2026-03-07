package com.healthsync.healthsync.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // ─── Constructors ─────────────────────────
    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email    = email;
        this.password = password;
    }

    // ─── Getters & Setters ────────────────────
    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getPassword()           { return password; }
    public void setPassword(String p)     { this.password = p; }
}