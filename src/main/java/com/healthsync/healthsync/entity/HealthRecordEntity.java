package com.healthsync.healthsync.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Entity
@Table(name = "health_records")
public class HealthRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ CRITICAL — needed for POST /api/health-records (body includes userId)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull(message = "Date is required")
    @Column(name = "record_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    // Vitals — all optional, at least one should be filled
    private Double weight;          // kg

    private String bloodPressure;   // e.g. "120/80"

    private Double sugarLevel;      // mg/dL

    private String recordType;      // General | Diabetes | Heart | Fitness

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ─── Constructors ─────────────────────────
    public HealthRecordEntity() {}

    // ─── Getters & Setters ────────────────────
    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public Long getUserId()                    { return userId; }
    public void setUserId(Long userId)         { this.userId = userId; }

    public LocalDate getRecordDate()           { return recordDate; }
    public void setRecordDate(LocalDate d)     { this.recordDate = d; }

    public Double getWeight()                  { return weight; }
    public void setWeight(Double weight)       { this.weight = weight; }

    public String getBloodPressure()           { return bloodPressure; }
    public void setBloodPressure(String bp)    { this.bloodPressure = bp; }

    public Double getSugarLevel()              { return sugarLevel; }
    public void setSugarLevel(Double s)        { this.sugarLevel = s; }

    public String getRecordType()              { return recordType; }
    public void setRecordType(String t)        { this.recordType = t; }

    public String getNotes()                   { return notes; }
    public void setNotes(String notes)         { this.notes = notes; }
}