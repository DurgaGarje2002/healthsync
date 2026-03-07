package com.healthsync.healthsync.service;

import com.healthsync.healthsync.entity.UserEntity;
import com.healthsync.healthsync.repository.AppointmentRepository;
import com.healthsync.healthsync.repository.HealthRecordRepository;
import com.healthsync.healthsync.repository.PatientCardRepository;
import com.healthsync.healthsync.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository          userRepository;
    private final PasswordEncoder         passwordEncoder;
    private final HealthRecordRepository  healthRecordRepository;
    private final PatientCardRepository   patientCardRepository;
    private final AppointmentRepository   appointmentRepository;

    public UserService(UserRepository         userRepository,
                       PasswordEncoder        passwordEncoder,
                       HealthRecordRepository healthRecordRepository,
                       PatientCardRepository  patientCardRepository,
                       AppointmentRepository  appointmentRepository) {
        this.userRepository         = userRepository;
        this.passwordEncoder        = passwordEncoder;
        this.healthRecordRepository = healthRecordRepository;
        this.patientCardRepository  = patientCardRepository;
        this.appointmentRepository  = appointmentRepository;
    }

    public UserEntity saveUser(UserEntity user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        }
        return userRepository.save(user);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public UserEntity updateUser(Long id, UserEntity updatedData) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (updatedData.getName()        != null) user.setName(updatedData.getName());
        if (updatedData.getPhone()       != null) user.setPhone(updatedData.getPhone());
        if (updatedData.getDateOfBirth() != null) user.setDateOfBirth(updatedData.getDateOfBirth());
        if (updatedData.getGender()      != null) user.setGender(updatedData.getGender());
        if (updatedData.getBloodGroup()  != null) user.setBloodGroup(updatedData.getBloodGroup());
        if (updatedData.getAddress()     != null) user.setAddress(updatedData.getAddress());
        return userRepository.save(user);
    }

    public UserEntity updateUserRole(Long id, String role) {
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        user.setRole(role);
        return userRepository.save(user);
    }

    // ── DELETE USER + ALL CHILD RECORDS ──────────────────────────
    // Uses deleteByUserId() — Spring Data generates a single DELETE WHERE
    // statement per table, executed in order before the user row is removed.
    // ✅ Fixes: FK constraint fails on appointments / patient_card / health_records
    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) return false;

        // Step 1: delete appointments (FK: appointments.user_id → users.id)
        appointmentRepository.deleteById(id);

        // Step 2: delete health records (FK: health_records.user_id → users.id)
        healthRecordRepository.deleteById(id);

        // Step 3: delete patient card (FK: patient_card.user_id → users.id)
        patientCardRepository.deleteById(id);

        // Step 4: now safe to delete the user row
        userRepository.deleteById(id);
        return true;
    }
}