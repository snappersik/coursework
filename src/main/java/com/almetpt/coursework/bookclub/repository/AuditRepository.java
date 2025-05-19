package com.almetpt.coursework.bookclub.repository;

import com.almetpt.coursework.bookclub.model.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
    List<AuditEntry> findAllByUserIdentifier(String userIdentifier);
}
