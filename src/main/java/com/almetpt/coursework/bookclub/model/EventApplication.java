package com.almetpt.coursework.bookclub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "event_applications_seq", allocationSize = 1)
public class EventApplication extends GenericModel {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    @Column(name = "attended")
    private boolean attended;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason")
    private RejectionReason rejectionReason;

    public enum RejectionReason {
        NO_CAPACITY("Нет свободных мест"),
        EVENT_CANCELLED("Мероприятие отменено"),
        OTHER("Другая причина");

        RejectionReason(String description) {
        }
    }
}