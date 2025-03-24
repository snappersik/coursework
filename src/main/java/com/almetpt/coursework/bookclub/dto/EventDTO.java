package com.almetpt.coursework.bookclub.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO extends GenericDTO {
    private Long id;
    private String title;
    private String eventType;
    private LocalDateTime date;
    private String description;
    private Integer maxParticipants;
}
