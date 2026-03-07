package com.healthsync.healthsync.controller;

import com.healthsync.healthsync.dto.ScanRequest;
import com.healthsync.healthsync.entity.PatientCardEntity;
import com.healthsync.healthsync.service.EmergencySmsService;
import com.healthsync.healthsync.service.PatientCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyScanController {

    private final PatientCardService patientCardService;
    private final EmergencySmsService emergencySmsService;

    public EmergencyScanController(
            PatientCardService patientCardService,
            EmergencySmsService emergencySmsService) {
        this.patientCardService = patientCardService;
        this.emergencySmsService = emergencySmsService;
    }

    /**
     * STEP 1 — Fires immediately on page load.
     * Sends WhatsApp alerts right away (no location yet).
     * 5-minute cooldown prevents spam.
     */
    @PostMapping("/scan")
    public ResponseEntity<?> handleScan(@RequestBody ScanRequest request) {

        System.out.println("=== /api/emergency/scan HIT ===");

        if (request.getPublicId() == null) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        PatientCardEntity card = patientCardService.getCardByPublicId(request.getPublicId());
        if (card == null) return ResponseEntity.notFound().build();

        System.out.println("Card found for patient: " + card.getUser().getName());

        long now = System.currentTimeMillis();
        long cooldownMillis = 5 * 60 * 1000;

        if (card.getLastScanTime() != null &&
                (now - card.getLastScanTime()) < cooldownMillis) {
            long remaining = (cooldownMillis - (now - card.getLastScanTime())) / 1000;
            System.out.println("COOLDOWN ACTIVE — Remaining: " + remaining + "s");
            return ResponseEntity.ok("Scan ignored (cooldown active)");
        }

        card.setLastScanTime(now);
        card.setScanCount(card.getScanCount() == null ? 1 : card.getScanCount() + 1);

        String locationLink = buildLocationLink(request.getLatitude(), request.getLongitude());
        if (locationLink != null) card.setLastScanLocation(locationLink);

        patientCardService.saveOrUpdate(card.getUser().getId(), card);

        String patientName = card.getUser().getName();
        sendIfPresent(card.getEmergencyContact(), patientName, locationLink);
        sendIfPresent(card.getContact2Phone(),    patientName, locationLink);
        sendIfPresent(card.getContact3Phone(),    patientName, locationLink);

        System.out.println("=== Emergency scan complete ===");
        return ResponseEntity.ok("Emergency alert triggered");
    }

    /**
     * STEP 2 — Called ~3 seconds later once browser gets GPS coords.
     * Sends a second WhatsApp message with the exact Google Maps link.
     * No cooldown — this is purely a location follow-up.
     */
    @PostMapping("/location-update")
    public ResponseEntity<?> updateLocation(@RequestBody ScanRequest request) {

        System.out.println("=== /api/emergency/location-update HIT ===");

        if (request.getPublicId() == null ||
            request.getLatitude() == null ||
            request.getLongitude() == null) {
            return ResponseEntity.badRequest().body("Missing data");
        }

        PatientCardEntity card = patientCardService.getCardByPublicId(request.getPublicId());
        if (card == null) return ResponseEntity.notFound().build();

        String locationLink = buildLocationLink(request.getLatitude(), request.getLongitude());
        card.setLastScanLocation(locationLink);
        patientCardService.saveOrUpdate(card.getUser().getId(), card);

        String patientName = card.getUser().getName();
        String msg = "📍 *Location Update*\n\n" +
                     "Patient *" + patientName + "* is here:\n" +
                     locationLink + "\n\n" +
                     "Please respond immediately.";

        sendRaw(card.getEmergencyContact(), msg);
        sendRaw(card.getContact2Phone(),    msg);
        sendRaw(card.getContact3Phone(),    msg);

        System.out.println("✅ Location update sent: " + locationLink);
        return ResponseEntity.ok("Location update sent");
    }

    /* ── Helpers ── */

    private String buildLocationLink(Double lat, Double lng) {
        if (lat != null && lng != null)
            return "https://maps.google.com/?q=" + lat + "," + lng;
        return null;
    }

    private void sendIfPresent(String phone, String patientName, String location) {
        if (phone != null && !phone.isBlank()) {
            try {
                emergencySmsService.sendEmergencySMS(phone, patientName, location);
                System.out.println("✅ SMS sent to: " + phone);
            } catch (Exception e) {
                System.out.println("❌ FAILED to: " + phone + " — " + e.getMessage());
            }
        }
    }

    private void sendRaw(String phone, String message) {
        if (phone != null && !phone.isBlank()) {
            try {
                emergencySmsService.sendRawMessage(phone, message);
                System.out.println("✅ Location update to: " + phone);
            } catch (Exception e) {
                System.out.println("❌ FAILED location update to: " + phone);
            }
        }
    }
}