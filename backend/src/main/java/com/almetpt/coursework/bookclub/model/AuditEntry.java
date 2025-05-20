package com.almetpt.coursework.bookclub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_entries", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action_type", columnList = "action_type"),
    @Index(name = "idx_audit_user_email", columnList = "user_email"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_type", nullable = false)
    private String actionType; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT

    @Column(name = "resource_type")
    private String resourceType; // BOOK, USER, ORDER, EVENT

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "details", length = 1000)
    private String details;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
