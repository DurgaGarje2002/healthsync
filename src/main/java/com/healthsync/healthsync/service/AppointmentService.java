package com.healthsync.healthsync.service;

import com.healthsync.healthsync.entity.AppointmentEntity;
import com.healthsync.healthsync.entity.AppointmentStatus;
import com.healthsync.healthsync.exception.ResourceNotFoundException;
import com.healthsync.healthsync.repository.AppointmentRepository;
import com.healthsync.healthsync.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository        = userRepository;
    }

    // ─── Book Appointment ─────────────────────
    public AppointmentEntity bookAppointment(Long userId, AppointmentEntity appointment) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        appointment.setUserId(userId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    // ─── Get By User ──────────────────────────
    public List<AppointmentEntity> getAppointmentsByUser(Long userId) {
        return appointmentRepository.findByUserIdOrderByAppointmentDateDesc(userId);
    }

    // ─── Update Status ────────────────────────
    public AppointmentEntity updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}