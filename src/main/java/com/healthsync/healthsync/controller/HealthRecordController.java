package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.HealthRecordEntity;
import com.healthsync.healthsync.service.HealthRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/health-records/user/{userId}
    // Called by: emergency-card-setup, edit-emergency-card
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/user/{userId}")
    public ResponseEntity<HealthRecordEntity> addHealthRecordByPath(
            @PathVariable Long userId,
            @Valid @RequestBody HealthRecordEntity record) {
        HealthRecordEntity savedRecord = healthRecordService.addHealthRecord(userId, record);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/health-records        ← ✅ ADDED: dashboard.js posts here
    // Body must include: { "userId": 1, ... }
    // ─────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<HealthRecordEntity> addHealthRecord(
            @Valid @RequestBody HealthRecordEntity record) {
        if (record.getUserId() == null) {
            return ResponseEntity.badRequest().build();
        }
        HealthRecordEntity savedRecord = healthRecordService.addHealthRecord(record.getUserId(), record);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/health-records/user/{userId}
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HealthRecordEntity>> getRecordsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(healthRecordService.getRecordsByUser(userId));
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/health-records          (admin — all records)
    // ─────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<HealthRecordEntity>> getAllRecords() {
        return ResponseEntity.ok(healthRecordService.getAllRecords());
    }

    // ─────────────────────────────────────────────────────────────────
    // PUT /api/health-records/{recordId}
    // ─────────────────────────────────────────────────────────────────
    @PutMapping("/{recordId}")
    public ResponseEntity<HealthRecordEntity> updateHealthRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody HealthRecordEntity updatedRecord) {
        return ResponseEntity.ok(
                healthRecordService.updateHealthRecord(recordId, updatedRecord));
    }

    // ─────────────────────────────────────────────────────────────────
    // DELETE /api/health-records/{recordId}
    // ─────────────────────────────────────────────────────────────────
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long recordId) {
        healthRecordService.deleteRecord(recordId);
        return ResponseEntity.noContent().build();
    }
}