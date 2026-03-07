package com.healthsync.healthsync.repository;

import com.healthsync.healthsync.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByUserIdOrderByAppointmentDateDesc(Long userId);

    // ✅ Bulk delete
    @Transactional
    void deleteByUserId(Long userId);
}