package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.model.AuditEntry;
import com.almetpt.coursework.bookclub.repository.AuditRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rest/audit")
public class AuditController {

    private final AuditRepository auditRepository;

    public AuditController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditEntry>> getAllAuditEntries() {
        List<AuditEntry> auditEntries = auditRepository.findAll();
        return ResponseEntity.ok(auditEntries);
    }
}