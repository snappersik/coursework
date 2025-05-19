package com.almetpt.coursework.bookclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
public abstract class GenericDTO {
    protected Long id;
    protected String createdBy;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm")
    protected LocalDateTime createdWhen;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm")
    protected LocalDateTime deletedWhen;

    protected String deletedBy;
    protected boolean isDeleted;
}