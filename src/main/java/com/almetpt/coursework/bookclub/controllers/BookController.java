package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rest/books")
@Tag(name = "Книги", description = "Контроллер для работы с книгами")
public class BookController extends GenericController<Book, BookDTO> {

    public BookController(BookService bookService) {
        super(bookService);
    }
}
