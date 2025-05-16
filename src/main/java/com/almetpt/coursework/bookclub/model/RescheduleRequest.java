package com.almetpt.coursework.bookclub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RescheduleRequest {
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    private LocalDateTime newDate;
    private String reason;
}
