package com.healthsync.healthsync.dto;

/**
 * Public DTO for emergency card — returned by /api/patient-card/public/{publicId}
 * NO auth required (scanned by emergency responders).
 * Must include ALL fields shown on the card.
 */
public class PatientCardPublicDTO {

    private String publicId;
    private String patientName;
    private String riskLevel;

    // Medical
    private String bloodType;
    private String currentDisease;
    private String allergies;
    private String medications;

    // Emergency contact
    private String contactName;
    private String relationship;
    private String emergencyContact;
    private String contact2Name;
    private String contact2Relationship;
    private String contact2Phone;
    private String contact3Name;
    private String contact3Relationship;
    private String contact3Phone;

    // Hospital
    private String hospitalName;
    private String hospitalAddress;
    private String hospitalMapLink;

    // ── Constructors ──────────────────────────────────────
    public PatientCardPublicDTO() {}

    public PatientCardPublicDTO(
            String publicId,
            String patientName,
            String riskLevel,
            String bloodType,
            String currentDisease,
            String allergies,
            String medications,
            String contactName,
            String relationship,
            String emergencyContact,
            String hospitalName,
            String hospitalAddress,
            String hospitalMapLink
    ) {
        this.publicId        = publicId;
        this.patientName     = patientName;
        this.riskLevel       = riskLevel;
        this.bloodType       = bloodType;
        this.currentDisease  = currentDisease;
        this.allergies       = allergies;
        this.medications     = medications;
        this.contactName     = contactName;
        this.relationship    = relationship;
        this.emergencyContact = emergencyContact;
        this.hospitalName    = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.hospitalMapLink = hospitalMapLink;
    }

    // ── Getters & Setters ─────────────────────────────────
    public String getPublicId()         { return publicId; }
    public void   setPublicId(String v) { this.publicId = v; }

    public String getPatientName()         { return patientName; }
    public void   setPatientName(String v) { this.patientName = v; }

    public String getRiskLevel()         { return riskLevel; }
    public void   setRiskLevel(String v) { this.riskLevel = v; }

    public String getBloodType()         { return bloodType; }
    public void   setBloodType(String v) { this.bloodType = v; }

    public String getCurrentDisease()         { return currentDisease; }
    public void   setCurrentDisease(String v) { this.currentDisease = v; }

    public String getAllergies()         { return allergies; }
    public void   setAllergies(String v) { this.allergies = v; }

    public String getMedications()         { return medications; }
    public void   setMedications(String v) { this.medications = v; }

    public String getContactName()         { return contactName; }
    public void   setContactName(String v) { this.contactName = v; }

    public String getRelationship()         { return relationship; }
    public void   setRelationship(String v) { this.relationship = v; }

    public String getEmergencyContact()         { return emergencyContact; }
    public void   setEmergencyContact(String v) { this.emergencyContact = v; }

    public String getContact2Name()             { return contact2Name; }
    public void   setContact2Name(String v)     { this.contact2Name = v; }

    public String getContact2Relationship()         { return contact2Relationship; }
    public void   setContact2Relationship(String v) { this.contact2Relationship = v; }

    public String getContact2Phone()            { return contact2Phone; }
    public void   setContact2Phone(String v)    { this.contact2Phone = v; }

    public String getContact3Name()             { return contact3Name; }
    public void   setContact3Name(String v)     { this.contact3Name = v; }

    public String getContact3Relationship()         { return contact3Relationship; }
    public void   setContact3Relationship(String v) { this.contact3Relationship = v; }

    public String getContact3Phone()            { return contact3Phone; }
    public void   setContact3Phone(String v)    { this.contact3Phone = v; }

    public String getHospitalName()         { return hospitalName; }
    public void   setHospitalName(String v) { this.hospitalName = v; }

    public String getHospitalAddress()         { return hospitalAddress; }
    public void   setHospitalAddress(String v) { this.hospitalAddress = v; }

    public String getHospitalMapLink()         { return hospitalMapLink; }
    public void   setHospitalMapLink(String v) { this.hospitalMapLink = v; }
}