package com.healthsync.healthsync.repository;

import com.healthsync.healthsync.entity.HealthRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecordEntity, Long> {
    List<HealthRecordEntity> findByUserIdOrderByRecordDateDesc(Long userId);

    // ✅ Bulk delete — faster and avoids Hibernate flush ordering issues
    @Transactional
    void deleteByUserId(Long userId);
}