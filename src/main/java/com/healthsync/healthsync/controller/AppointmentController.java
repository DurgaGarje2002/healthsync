package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.AppointmentEntity;
import com.healthsync.healthsync.entity.AppointmentStatus;
import com.healthsync.healthsync.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Book appointment
    @PostMapping("/user/{userId}")
    public ResponseEntity<AppointmentEntity> bookAppointment(
            @PathVariable Long userId,
            @Valid @RequestBody AppointmentEntity appointment) {
        return ResponseEntity.ok(
                appointmentService.bookAppointment(userId, appointment));
    }

    // Get appointments of a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AppointmentEntity>> getAppointmentsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByUser(userId));
    }

    // Update appointment status — JS sends JSON body: { "status": "COMPLETED" }
    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentEntity> updateStatus(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, String> body) {
        AppointmentStatus status = AppointmentStatus.valueOf(
                body.get("status").toUpperCase());
        return ResponseEntity.ok(
                appointmentService.updateAppointmentStatus(appointmentId, status));
    }
}