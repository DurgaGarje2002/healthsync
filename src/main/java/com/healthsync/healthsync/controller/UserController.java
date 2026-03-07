package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.UserEntity;
import com.healthsync.healthsync.repository.UserRepository;
import com.healthsync.healthsync.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * UserController  —  /api/users
 * ─────────────────────────────────────────────────────────────
 * REPLACE your existing UserController.java with this file.
 *
 * Key changes:
 *  - PUT /{id} accepts flexible Map body (profile.html sends
 *    name, phone, dateOfBirth, gender, bloodGroup)
 *  - DELETE /{id} now cascade-deletes via UserService
 *    (fixes: FK constraint fails on appointments/health_records)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService    userService;
    private final UserRepository userRepository;

    public UserController(UserService    userService,
                          UserRepository userRepository) {
        this.userService    = userService;
        this.userRepository = userRepository;
    }

    // ── GET /api/users  (admin — all users) ──────────────────────
    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── GET /api/users/{id} ───────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserEntity user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    // ── POST /api/users ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        return ResponseEntity.ok(userService.saveUser(user));
    }

    // ── PUT /api/users/{id}  (profile update from profile.html) ──
    // Accepts Map — only updates fields that are provided.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        UserEntity user = userService.getUserById(id);
        if (user == null) return ResponseEntity.notFound().build();

        if (body.containsKey("name") && body.get("name") != null)
            user.setName(body.get("name").trim());
        if (body.containsKey("phone"))
            user.setPhone(body.get("phone"));
        if (body.get("dateOfBirth") != null && !body.get("dateOfBirth").isBlank()) {
            user.setDateOfBirth(LocalDate.parse(body.get("dateOfBirth")));
        }
        if (body.containsKey("gender"))
            user.setGender(body.get("gender"));
        if (body.containsKey("bloodGroup"))
            user.setBloodGroup(body.get("bloodGroup"));
        if (body.containsKey("address"))
            user.setAddress(body.get("address"));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    // ── DELETE /api/users/{id} ────────────────────────────────────
    // ✅ FIX: UserService.deleteUser() now deletes appointments,
    //    health_records, and patient_card BEFORE deleting the user
    //    — prevents MySQL FK constraint error
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (!deleted) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}