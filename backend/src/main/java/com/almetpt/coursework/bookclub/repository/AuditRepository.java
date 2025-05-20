package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
    
    // Оптимизированный запрос с динамическими условиями
    @Query("SELECT a FROM AuditEntry a WHERE " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:userEmail IS NULL OR a.userEmail = :userEmail) AND " +
           "(:dateFrom IS NULL OR a.timestamp >= :dateFrom) AND " +
           "(:dateTo IS NULL OR a.timestamp <= :dateTo)")
    Page<AuditEntry> findByFilters(
            @Param("actionType") String actionType,
            @Param("userEmail") String userEmail,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
    
    // Дополнительные оптимизированные запросы для частых сценариев
    Page<AuditEntry> findByActionType(String actionType, Pageable pageable);
    
    Page<AuditEntry> findByUserEmail(String userEmail, Pageable pageable);
    
    Page<AuditEntry> findByTimestampBetween(LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);
    
    Page<AuditEntry> findByActionTypeAndUserEmail(String actionType, String userEmail, Pageable pageable);
}
