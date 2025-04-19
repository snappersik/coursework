package com.almetpt.coursework.bookclub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "books_seq", allocationSize = 1)
public class Book extends GenericModel {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookGenre genre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_reading")
    private boolean isReading;

    private LocalDate publicationDate;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "cover_image_filename")
    private String coverImageFilename;

    @Column(name = "original_cover_image_filename")
    private String originalCoverImageFilename;

    @Lob
    @Column(name = "cover_image_data")
    private byte[] coverImageData;

    // Вспомогательный метод для определения наличия локального изображения
    public boolean hasLocalImage() {
        return (coverImageFilename != null && !coverImageFilename.isBlank())
                || (coverImageData != null && coverImageData.length > 0);
    }

    @OneToMany(mappedBy = "book")
    private List<Event> events;

}
