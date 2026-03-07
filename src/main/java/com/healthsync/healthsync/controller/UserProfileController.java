package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.UserEntity;
import com.healthsync.healthsync.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserProfileController  —  /api/users
 * ──────────────────────────────────────────────────────────────
 * NOTE: GET /api/users/{id}, PUT /api/users/{id}, and
 *       DELETE /api/users/{id} are already in your UserController.
 *       This class ONLY adds the password-change endpoint.
 *
 *   PUT /api/users/{id}/password  — change password (profile.html)
 */
@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserService userService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.healthsync.healthsync.repository.UserRepository userRepository;

    public UserProfileController(
            UserService userService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            com.healthsync.healthsync.repository.UserRepository userRepository) {
        this.userService     = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository  = userRepository;
    }

    // ── PUT /api/users/{id}/password ──────────────────────────────
    // Body: { "currentPassword": "...", "newPassword": "..." }
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Both fields are required"));

        if (newPassword.length() < 6)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password must be at least 6 characters"));

        UserEntity user = userService.getUserById(id);
        if (user == null)
            return ResponseEntity.notFound().build();

        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Current password is incorrect"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}