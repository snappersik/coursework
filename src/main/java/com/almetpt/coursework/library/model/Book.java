package com.almetpt.coursework.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "default_generator", sequenceName = "books_sequence", allocationSize = 1)
public class Book extends GenericModel {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "status", nullable = false)
    private String status; // Прочитано или читается сейчас

    @Lob
    @Column(name = "image")
    private byte[] image; // Поле для хранения изображения

    // Связь с пользователем (многие ко многим через промежуточную таблицу UserBooks)
    @ManyToMany(mappedBy = "books")
    private List<User> users;
}
