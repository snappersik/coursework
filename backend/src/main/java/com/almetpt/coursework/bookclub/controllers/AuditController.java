package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.model.AuditEntry;
import com.almetpt.coursework.bookclub.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rest/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    private static final Logger log = LoggerFactory.getLogger(AuditController.class);
    private final AuditRepository auditRepository;

    public AuditController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping
    public ResponseEntity<Page<AuditEntry>> getAllAuditEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        
        log.debug("Fetching audit entries with params: page={}, size={}, actionType={}, userEmail={}, dateFrom={}, dateTo={}",
                page, size, actionType, userEmail, dateFrom, dateTo);

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditEntry> auditEntries;

        if (actionType != null || userEmail != null || dateFrom != null || dateTo != null) {
            auditEntries = auditRepository.findByFilters(actionType, userEmail, dateFrom, dateTo, pageable);
        } else {
            auditEntries = auditRepository.findAll(pageable);
        }

        return ResponseEntity.ok(auditEntries);
    }
}
