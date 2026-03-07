package com.healthsync.healthsync.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_cards")
public class PatientCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "public_id", unique = true)
    private String publicId;

    @Column(name = "last_scan_time")
    private Long lastScanTime;
    
    @Column(name = "scan_count")
    private Integer scanCount;

    @Column(name = "last_scan_location")
    private String lastScanLocation;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

    // ── Risk ─────────────────────────────────
    @Column(name = "risk_level")
    private String riskLevel;

    // ── Medical ──────────────────────────────
    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "current_disease", columnDefinition = "TEXT")
    private String currentDisease;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "medications", columnDefinition = "TEXT")
    private String medications;

    // ── Emergency Contact 1 ──────────────────
    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    // ── Emergency Contact 2 ──────────────────
    @Column(name = "contact2_name")
    private String contact2Name;

    @Column(name = "contact2_relationship")
    private String contact2Relationship;

    @Column(name = "contact2_phone")
    private String contact2Phone;

    // ── Emergency Contact 3 ──────────────────
    @Column(name = "contact3_name")
    private String contact3Name;

    @Column(name = "contact3_relationship")
    private String contact3Relationship;

    @Column(name = "contact3_phone")
    private String contact3Phone;

    // ── Hospital ─────────────────────────────
    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "hospital_address", columnDefinition = "TEXT")
    private String hospitalAddress;

    @Column(name = "hospital_map_link", columnDefinition = "TEXT")
    private String hospitalMapLink;

    // ── Lifecycle (ONLY ONE PrePersist) ─────
    @PrePersist
    public void prePersist() {

        if (this.publicId == null || this.publicId.isBlank()) {
            this.publicId = UUID.randomUUID().toString();
        }

        if (this.lastScanTime == null) {
            this.lastScanTime = 0L;
        }

        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    // ── Getters & Setters ────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public Long getLastScanTime() { return lastScanTime; }
    public void setLastScanTime(Long lastScanTime) { this.lastScanTime = lastScanTime; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getCurrentDisease() { return currentDisease; }
    public void setCurrentDisease(String currentDisease) { this.currentDisease = currentDisease; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedications() { return medications; }
    public void setMedications(String medications) { this.medications = medications; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getContact2Name() { return contact2Name; }
    public void setContact2Name(String contact2Name) { this.contact2Name = contact2Name; }

    public String getContact2Relationship() { return contact2Relationship; }
    public void setContact2Relationship(String contact2Relationship) { this.contact2Relationship = contact2Relationship; }

    public String getContact2Phone() { return contact2Phone; }
    public void setContact2Phone(String contact2Phone) { this.contact2Phone = contact2Phone; }

    public String getContact3Name() { return contact3Name; }
    public void setContact3Name(String contact3Name) { this.contact3Name = contact3Name; }

    public String getContact3Relationship() { return contact3Relationship; }
    public void setContact3Relationship(String contact3Relationship) { this.contact3Relationship = contact3Relationship; }

    public String getContact3Phone() { return contact3Phone; }
    public void setContact3Phone(String contact3Phone) { this.contact3Phone = contact3Phone; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }

    public String getHospitalMapLink() { return hospitalMapLink; }
    public void setHospitalMapLink(String hospitalMapLink) { this.hospitalMapLink = hospitalMapLink; }

    public Integer getScanCount() {
        return scanCount;
    }

    public void setScanCount(Integer scanCount) {
        this.scanCount = scanCount;
    }

    public String getLastScanLocation() {
        return lastScanLocation;
    }

    public void setLastScanLocation(String lastScanLocation) {
        this.lastScanLocation = lastScanLocation;
    }

}