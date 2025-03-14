package com.almetpt.coursework.BookClub.dto;

import com.almetpt.coursework.BookClub.model.Genre;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO extends GenericDTO {
    private Long id;
    private String title;
    private String author;
    private String description;
    private Genre genre;
    private boolean isReading;
}
