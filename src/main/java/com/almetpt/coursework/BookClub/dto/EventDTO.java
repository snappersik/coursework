package com.almetpt.coursework.BookClub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO extends GenericDTO {
    private Long id;
    private String title;
    private Date date;
    private String description;
}