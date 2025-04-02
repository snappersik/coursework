package com.almetpt.coursework.bookclub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm")
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