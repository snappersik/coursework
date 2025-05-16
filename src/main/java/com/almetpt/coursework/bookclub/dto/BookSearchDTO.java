package com.almetpt.coursework.bookclub.dto;

import com.almetpt.coursework.bookclub.model.BookGenre;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookSearchDTO {
    private String bookTitle;
    private String authorName;
    private BookGenre genre;
}
