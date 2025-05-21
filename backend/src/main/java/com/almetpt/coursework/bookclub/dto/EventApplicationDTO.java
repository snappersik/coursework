package com.almetpt.coursework.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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