package com.healthsync.healthsync.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest DTO
 * ────────────────────
 * Separates API input validation from the UserEntity JPA constraints.
 * Used by POST /api/auth/register
 *
 * WHY THIS IS NEEDED:
 * Using @RequestBody UserEntity directly exposes JPA constraints (@Column, @Table etc)
 * to the API layer and causes issues when entity fields don't match the incoming JSON.
 */
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // ─── Constructors ─────────────────────────
    public RegisterRequest() {}

    // ─── Getters & Setters ────────────────────
    public String getName()                   { return name; }
    public void setName(String name)          { this.name = name; }

    public String getEmail()                  { return email; }
    public void setEmail(String email)        { this.email = email; }

    public String getPassword()               { return password; }
    public void setPassword(String password)  { this.password = password; }
}