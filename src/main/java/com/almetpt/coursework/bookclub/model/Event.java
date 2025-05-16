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
@SequenceGenerator(name = "default_generator", sequenceName = "events_seq", allocationSize = 1)
public class Event extends GenericModel {

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

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

}