package com.almetpt.coursework.bookclub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "slider_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "slider_books_seq", allocationSize = 1)
public class SliderBook extends GenericModel {

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(columnDefinition = "TEXT", name = "custom_description")
    private String customDescription;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "background_image_url")
    private String backgroundImageUrl;

    @Column(name = "background_image_filename")
    private String backgroundImageFilename;

    @Lob
    @Column(name = "background_image_data")
    private byte[] backgroundImageData;

    // Вспомогательный метод для определения наличия локального фонового изображения
    public boolean hasLocalBackgroundImage() {
        return (backgroundImageFilename != null && !backgroundImageFilename.isBlank())
                || (backgroundImageData != null && backgroundImageData.length > 0);
    }

    // Метод получения итогового описания (кастомное или стандартное от книги)
    public String getDescription() {
        return (customDescription != null && !customDescription.isBlank()) 
               ? customDescription 
               : (book != null ? book.getDescription() : null);
    }
}
