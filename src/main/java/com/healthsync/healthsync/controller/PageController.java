package com.healthsync.healthsync.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {

    // ─── Landing / Home ──────────────────────
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // ─── Auth Pages ──────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    // ─── Dashboard ───────────────────────────
    @GetMapping("/dashboard")
    public String userDashboard() {
        return "dashboard";
    }

    
    @GetMapping("/admin-dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }
    
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    // ─── Emergency Card Setup ────────────────
    @GetMapping("/emergency-card-setup")
    public String emergencyCardSetup() {
        return "emergency-card-setup";
    }

    @GetMapping("/edit-card")
    public String editEmergencyCard() {
        return "edit-card";
    }

    // ─── Static Pages ─────────────────────────
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/feedback")
    public String feedbackPage() {
        return "feedback";
    }

    @GetMapping("/reminders")
    public String remindersPage() {
        return "reminders";
    }

    @GetMapping("/health-report")
    public String healthReportPage() {
        return "health-report";
    }
}