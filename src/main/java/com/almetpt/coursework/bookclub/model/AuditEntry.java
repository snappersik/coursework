package com.almetpt.coursework.bookclub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    // Заменяем Long userId на String userIdentifier
    private String userIdentifier;

    private LocalDateTime timestamp;

    @Column(length = 200)
    private String details;

    public enum ActionType {
        CREATE_EVENT, DELETE_EVENT, CANCEL_EVENT, RESCHEDULE_EVENT, UPDATE_EVENT_APPLICATION, MANAGE_USER, VIEW_CART
    }
}
