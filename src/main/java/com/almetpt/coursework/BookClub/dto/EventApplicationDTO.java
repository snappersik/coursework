package com.almetpt.coursework.BookClub.dto;

import com.almetpt.coursework.BookClub.model.ApplicationStatus;
import com.almetpt.coursework.BookClub.model.OrderStatus;
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
    private ApplicationStatus applicationStatus;
}
