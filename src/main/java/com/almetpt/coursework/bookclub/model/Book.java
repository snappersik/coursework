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
public class Book extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_generator")
    @SequenceGenerator(name = "book_generator", sequenceName = "book_seq", allocationSize = 1)
    @Column(name = "book_id")
    private Long id;

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

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @OneToMany(mappedBy = "book")
    private List<Event> events;

}
