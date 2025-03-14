package com.almetpt.coursework.BookClub.controllers;

import com.almetpt.coursework.BookClub.dto.BookDTO;
import com.almetpt.coursework.BookClub.model.Book;
import com.almetpt.coursework.BookClub.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rest/books")
@Tag(name = "Книги",
        description = "Контроллер для работы с книгами")
public class BookController
        extends GenericController<Book, BookDTO> {
    public BookController(BookService bookService) {
        super(bookService);
    }

}
