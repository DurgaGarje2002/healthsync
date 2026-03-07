package com.healthsync.healthsync.repository;

import com.healthsync.healthsync.entity.PatientCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientCardRepository extends JpaRepository<PatientCardEntity, Long> {

    Optional<PatientCardEntity> findByUser_Id(Long userId);

    Optional<PatientCardEntity> findByPublicId(String publicId);

}