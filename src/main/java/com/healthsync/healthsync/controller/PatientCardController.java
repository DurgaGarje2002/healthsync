package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.dto.PatientCardPublicDTO;
import com.healthsync.healthsync.entity.PatientCardEntity;
import com.healthsync.healthsync.entity.UserEntity;
import com.healthsync.healthsync.repository.UserRepository;
import com.healthsync.healthsync.service.PatientCardService;
import com.healthsync.healthsync.service.QRCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patient-card")
public class PatientCardController {

    private final PatientCardService patientCardService;
    private final QRCodeService qrCodeService;
    private final UserRepository userRepository;

    public PatientCardController(PatientCardService patientCardService,
                                 QRCodeService qrCodeService,
                                 UserRepository userRepository) {
        this.patientCardService = patientCardService;
        this.qrCodeService = qrCodeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCardByUser(@PathVariable Long userId) {
        PatientCardEntity card = patientCardService.getCardByUserId(userId);
        if (card == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> saveCard(@PathVariable Long userId,
                                      @RequestBody Map<String, String> body) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            PatientCardEntity card = patientCardService.getCardByUserId(userId);
            if (card == null) {
                card = new PatientCardEntity();
                card.setUser(user);
            }

            if (body.containsKey("riskLevel")) card.setRiskLevel(body.get("riskLevel"));
            if (body.containsKey("bloodType")) card.setBloodType(emptyToNull(body.get("bloodType")));
            if (body.containsKey("currentDisease")) card.setCurrentDisease(body.get("currentDisease"));
            if (body.containsKey("allergies")) card.setAllergies(body.get("allergies"));
            if (body.containsKey("medications")) card.setMedications(emptyToNull(body.get("medications")));
            if (body.containsKey("contactName")) card.setContactName(emptyToNull(body.get("contactName")));
            if (body.containsKey("relationship")) card.setRelationship(emptyToNull(body.get("relationship")));
            if (body.containsKey("emergencyContact")) card.setEmergencyContact(body.get("emergencyContact"));
            if (body.containsKey("contact2Name")) card.setContact2Name(emptyToNull(body.get("contact2Name")));
            if (body.containsKey("contact2Relationship")) card.setContact2Relationship(emptyToNull(body.get("contact2Relationship")));
            if (body.containsKey("contact2Phone")) card.setContact2Phone(emptyToNull(body.get("contact2Phone")));
            if (body.containsKey("contact3Name")) card.setContact3Name(emptyToNull(body.get("contact3Name")));
            if (body.containsKey("contact3Relationship")) card.setContact3Relationship(emptyToNull(body.get("contact3Relationship")));
            if (body.containsKey("contact3Phone")) card.setContact3Phone(emptyToNull(body.get("contact3Phone")));
            if (body.containsKey("hospitalName")) card.setHospitalName(body.get("hospitalName"));
            if (body.containsKey("hospitalAddress")) card.setHospitalAddress(body.get("hospitalAddress"));
            if (body.containsKey("hospitalMapLink")) card.setHospitalMapLink(emptyToNull(body.get("hospitalMapLink")));

            PatientCardEntity saved = patientCardService.saveOrUpdate(userId, card);

            return ResponseEntity.ok(
                    Map.of("publicId", saved.getPublicId())
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to save: " + e.getMessage()));
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}