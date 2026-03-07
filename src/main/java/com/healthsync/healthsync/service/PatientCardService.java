package com.healthsync.healthsync.service;

import com.healthsync.healthsync.entity.PatientCardEntity;
import com.healthsync.healthsync.repository.PatientCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientCardService {

    private final PatientCardRepository patientCardRepository;

    public PatientCardService(PatientCardRepository patientCardRepository) {
        this.patientCardRepository = patientCardRepository;
    }

    /* ─────────────────────────────────────
       Get card by USER ID
       Used in dashboard
    ───────────────────────────────────── */
    public PatientCardEntity getCardByUserId(Long userId) {

        return patientCardRepository
                .findByUser_Id(userId)
                .orElse(null);
    }


    /* ─────────────────────────────────────
       Get card by PUBLIC ID
       Used for:
       QR scan
       Physical card
       Emergency page
    ───────────────────────────────────── */
    public PatientCardEntity getCardByPublicId(String publicId) {

        return patientCardRepository
                .findByPublicId(publicId)
                .orElse(null);
    }


    /* ─────────────────────────────────────
       Save or update card
    ───────────────────────────────────── */
    @Transactional
    public PatientCardEntity saveOrUpdate(Long userId, PatientCardEntity card) {

        PatientCardEntity existing =
                patientCardRepository.findByUser_Id(userId).orElse(null);

        if (existing != null) {
            card.setId(existing.getId()); // keep same row
        }

        return patientCardRepository.save(card);
    }


    /* ─────────────────────────────────────
       Increase QR scan count
    ───────────────────────────────────── */
    @Transactional
    public void incrementScanCount(PatientCardEntity card) {

        if (card.getScanCount() == null) {
            card.setScanCount(1);
        } else {
            card.setScanCount(card.getScanCount() + 1);
        }

        patientCardRepository.save(card);
    }
}