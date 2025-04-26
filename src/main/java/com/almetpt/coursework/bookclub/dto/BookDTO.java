package com.almetpt.coursework.bookclub.dto;

import com.almetpt.coursework.bookclub.model.BookGenre;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

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
    
    // Добавляем @Builder.Default для инициализации пустого HashSet
    @Builder.Default
    private Set<BookGenre> genres = new HashSet<>();
    
    private boolean isReading;
    private String coverImageUrl;
    private String coverImageFilename;
    private boolean hasLocalImage;
}
