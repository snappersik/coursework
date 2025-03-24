package com.almetpt.coursework.bookclub.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@NoArgsConstructor
public abstract class GenericDTO {
    protected Long id;
    protected String createdBy;
    protected LocalDateTime createdWhen;
    protected LocalDateTime deletedWhen;
    protected String deletedBy;
    protected boolean isDeleted;
}