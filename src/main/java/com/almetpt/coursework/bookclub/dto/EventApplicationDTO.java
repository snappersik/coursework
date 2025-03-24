package com.almetpt.coursework.bookclub.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventApplicationDTO extends GenericDTO {
    private Long id;
    private Long userId;
    private Long eventId;
    private String applicationStatus;
}
