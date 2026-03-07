package com.healthsync.healthsync.service;

import com.healthsync.healthsync.entity.HealthRecordEntity;
import com.healthsync.healthsync.exception.ResourceNotFoundException;
import com.healthsync.healthsync.repository.HealthRecordRepository;
import com.healthsync.healthsync.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    public HealthRecordService(HealthRecordRepository healthRecordRepository,
                                UserRepository userRepository) {
        this.healthRecordRepository = healthRecordRepository;
        this.userRepository         = userRepository;
    }

    // ─── Add Record ───────────────────────────
    public HealthRecordEntity addHealthRecord(Long userId, HealthRecordEntity record) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        record.setUserId(userId);
        return healthRecordRepository.save(record);
    }

    // ─── Get By User ──────────────────────────
    public List<HealthRecordEntity> getRecordsByUser(Long userId) {
        return healthRecordRepository.findByUserIdOrderByRecordDateDesc(userId);
    }

    // ─── Get All (admin) ──────────────────────
    public List<HealthRecordEntity> getAllRecords() {
        return healthRecordRepository.findAll();
    }

    // ─── Update ───────────────────────────────
    public HealthRecordEntity updateHealthRecord(Long recordId, HealthRecordEntity updated) {
        HealthRecordEntity existing = healthRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthRecord", recordId));

        if (updated.getRecordDate()    != null) existing.setRecordDate(updated.getRecordDate());
        if (updated.getWeight()        != null) existing.setWeight(updated.getWeight());
        if (updated.getBloodPressure() != null) existing.setBloodPressure(updated.getBloodPressure());
        if (updated.getSugarLevel()    != null) existing.setSugarLevel(updated.getSugarLevel());
        if (updated.getRecordType()    != null) existing.setRecordType(updated.getRecordType());
        if (updated.getNotes()         != null) existing.setNotes(updated.getNotes());

        return healthRecordRepository.save(existing);
    }

    // ─── Delete ───────────────────────────────
    public void deleteRecord(Long recordId) {
        if (!healthRecordRepository.existsById(recordId)) {
            throw new ResourceNotFoundException("HealthRecord", recordId);
        }
        healthRecordRepository.deleteById(recordId);
    }
}