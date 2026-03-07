package com.healthsync.healthsync.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    @NotBlank(message = "Reason is required")
    @Column(columnDefinition = "TEXT")
    private String reason;

    @NotNull(message = "Appointment date is required")
    @Column(name = "appointment_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    private String location;        // optional clinic/hospital
    private String notes;           // optional notes

    // ─── Constructors ─────────────────────────
    public AppointmentEntity() {}

    // ─── Getters & Setters ────────────────────
    public Long getId()                             { return id; }
    public void setId(Long id)                      { this.id = id; }

    public Long getUserId()                         { return userId; }
    public void setUserId(Long userId)              { this.userId = userId; }

    public String getDoctorName()                   { return doctorName; }
    public void setDoctorName(String d)             { this.doctorName = d; }

    public String getReason()                       { return reason; }
    public void setReason(String reason)            { this.reason = reason; }

    public LocalDateTime getAppointmentDate()       { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime d) { this.appointmentDate = d; }

    public AppointmentStatus getStatus()            { return status; }
    public void setStatus(AppointmentStatus s)      { this.status = s; }

    public String getLocation()                     { return location; }
    public void setLocation(String location)        { this.location = location; }

    public String getNotes()                        { return notes; }
    public void setNotes(String notes)              { this.notes = notes; }
}