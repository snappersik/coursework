package com.almetpt.coursework.bookclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO extends GenericDTO {
    private Long id;
    private String title;
    private String eventType;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime date;
    private String description;
    private Integer maxParticipants;
    private boolean isCancelled;
    private String cancellationReason;
}
