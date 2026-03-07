package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.entity.PatientCardEntity;
import com.healthsync.healthsync.repository.PatientCardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmergencyPageController {

    private final PatientCardRepository patientCardRepository;

    public EmergencyPageController(PatientCardRepository patientCardRepository) {
        this.patientCardRepository = patientCardRepository;
    }

    // Opened from dashboard "View QR Card" button — shows printable ID card
    @GetMapping("/physical-card/{publicId}")
    public String showPhysicalCard(@PathVariable String publicId, Model model) {
        PatientCardEntity card = patientCardRepository.findByPublicId(publicId).orElse(null);
        if (card == null) {
            return "error";
        }
        model.addAttribute("card", card);
        model.addAttribute("publicId", publicId);
        return "physical-card";
    }

    // Opened when someone SCANS the QR code — shows full emergency details
    @GetMapping("/emergency/card/{publicId}")
    public String showEmergencyCard(@PathVariable String publicId, Model model) {
        PatientCardEntity card = patientCardRepository.findByPublicId(publicId).orElse(null);
        if (card == null) {
            return "error";
        }
        model.addAttribute("card", card);
        model.addAttribute("publicId", publicId);
        return "emergency-card-view";
    }
}