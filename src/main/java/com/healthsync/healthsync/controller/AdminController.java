package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.PatientCardEntity;
import com.healthsync.healthsync.entity.UserEntity;
import com.healthsync.healthsync.repository.PatientCardRepository;
import com.healthsync.healthsync.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final PatientCardRepository patientCardRepository;

    public AdminController(UserService userService,
                           PatientCardRepository patientCardRepository) {
        this.userService = userService;
        this.patientCardRepository = patientCardRepository;
    }

    // 1️⃣ Get all users
    @GetMapping("/users")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 2️⃣ Change user role
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<String> changeUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN"))) {
            return ResponseEntity.badRequest().body("❌ Invalid role. Use ROLE_USER or ROLE_ADMIN");
        }
        UserEntity updatedUser = userService.updateUserRole(userId, role);
        if (updatedUser == null) {
            return ResponseEntity.badRequest().body("❌ User not found");
        }
        return ResponseEntity.ok("✅ Role updated to " + role);
    }

    // 3️⃣ Delete user
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        boolean deleted = userService.deleteUser(userId);
        if (!deleted) {
            return ResponseEntity.badRequest().body("❌ User not found");
        }
        return ResponseEntity.ok("🗑 User deleted successfully");
    }

    // 4️⃣ Get admin stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<UserEntity> users = userService.getAllUsers();
        long totalUsers = users.size();
        long adminCount = users.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();
        long userCount  = totalUsers - adminCount;
        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "adminCount", adminCount,
            "userCount",  userCount
        ));
    }

    // 5️⃣ Get all emergency cards
    @GetMapping("/patient-cards")
    public ResponseEntity<?> getAllPatientCards() {
        try {
            List<PatientCardEntity> cards = patientCardRepository.findAll();
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}