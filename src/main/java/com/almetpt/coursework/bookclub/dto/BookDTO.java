package com.almetpt.coursework.bookclub.dto;

import com.almetpt.coursework.bookclub.model.BookGenre;

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
    private BookGenre genre;
    private boolean isReading;
    
    // Новые поля для изображений
    private String coverImageUrl;
    private boolean hasLocalImage;
}