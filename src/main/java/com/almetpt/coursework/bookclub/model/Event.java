package com.almetpt.coursework.bookclub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "event_type")
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "is_cancelled")
    private boolean isCancelled;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventApplication> applications;

}