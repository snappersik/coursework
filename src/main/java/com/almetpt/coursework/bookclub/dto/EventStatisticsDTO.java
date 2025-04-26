package com.almetpt.coursework.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventStatisticsDTO {
    private Long eventId;
    private String title;
    private String eventType;
    private int maxParticipants;
    private int approvedApplications;
    private int totalApplications;
    private double fillPercentage; // Процент заполненности
}
