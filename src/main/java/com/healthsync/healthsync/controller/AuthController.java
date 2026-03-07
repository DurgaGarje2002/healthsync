package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.dto.RegisterRequest;
import com.healthsync.healthsync.entity.UserEntity;
import com.healthsync.healthsync.repository.UserRepository;
import com.healthsync.healthsync.security.JwtUtil;
import com.healthsync.healthsync.security.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // Optional mail sender — null if spring.mail.* not configured
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8082}")
    private String appBaseUrl;

    // In-memory token store for forgot-password
    private static final ConcurrentHashMap<String, Object[]> resetTokenStore
            = new ConcurrentHashMap<>();

    public AuthController(UserRepository userRepository,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.jwtUtil         = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        // ✅ Case-insensitive email lookup — fixes "user not found" for wrong case
        String email = request.getEmail().trim().toLowerCase();

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token",  token);
        response.put("userId", user.getId());
        response.put("email",  user.getEmail());
        response.put("role",   user.getRole());
        // ✅ Fallback to email if name is null (for old accounts without name)
        response.put("name",   user.getName() != null ? user.getName() : user.getEmail());

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // ✅ Uses RegisterRequest DTO instead of UserEntity directly
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already registered"));
        }

        UserEntity user = new UserEntity();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        UserEntity saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token",  token);
        response.put("userId", saved.getId());
        response.put("email",  saved.getEmail());
        response.put("role",   saved.getRole());
        response.put("name",   saved.getName());

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/forgot-password
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        Optional<UserEntity> userOpt = userRepository
                .findByEmailIgnoreCase(email.trim().toLowerCase());

        if (userOpt.isPresent()) {
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);
            resetTokenStore.put(resetToken,
                    new Object[]{ email.trim().toLowerCase(), expiry });

            String resetLink = appBaseUrl + "/reset-password?token=" + resetToken;

            // ✅ Send real email via JavaMailSender
            if (mailSender != null) {
                try {
                    org.springframework.mail.SimpleMailMessage msg = new org.springframework.mail.SimpleMailMessage();
                    msg.setTo(email.trim());
                    msg.setSubject("HealthSync — Reset Your Password");
                    msg.setText(
                        "Hello " + userOpt.get().getName() + ",\n\n" +
                        "We received a request to reset your HealthSync password.\n\n" +
                        "Click the link below to set a new password (expires in 1 hour):\n" +
                        resetLink + "\n\n" +
                        "If you did not request this, please ignore this email.\n\n" +
                        "— The HealthSync Team"
                    );
                    mailSender.send(msg);
                } catch (Exception ex) {
                    // Log but don't expose error to client
                    System.err.println("[MAIL ERROR] Could not send reset email: " + ex.getMessage());
                    System.out.println("[FALLBACK] Reset link: " + resetLink);
                }
            } else {
                // Fallback: print to console if mail not configured
                System.out.println("=== PASSWORD RESET (mail not configured) ===");
                System.out.println("Link: " + resetLink);
            }
        }

        // Always 200 — don't reveal whether the email exists
        return ResponseEntity.ok(Map.of(
            "message", "If that email is registered, a reset link has been sent."
        ));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/auth/reset-password
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token       = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Reset token is required"));

        if (newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must be at least 6 characters"));

        Object[] data = resetTokenStore.get(token);
        if (data == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid or expired reset token"));

        String email = (String) data[0];
        LocalDateTime expiry = (LocalDateTime) data[1];

        if (LocalDateTime.now().isAfter(expiry)) {
            resetTokenStore.remove(token);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Reset token expired. Request a new one."));
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null)
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenStore.remove(token);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}