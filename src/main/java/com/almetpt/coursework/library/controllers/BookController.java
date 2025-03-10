package com.almetpt.coursework.library.controllers;

import com.almetpt.coursework.library.dto.ProductDTO;
import com.almetpt.coursework.library.model.Book;
import com.almetpt.coursework.library.service.ProductService;
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
        description = "Контроллер для работы с книгами библиотеки")
public class BookController
        extends GenericController<Book, ProductDTO> {
    public BookController(ProductService bookService) {
        super(bookService);
    }

    @Operation(description = "Добавить книгу к автору")
    @RequestMapping(value = "/addAuthor", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDTO> addAuthor(@RequestParam(value = "bookId") Long bookId,
                                                @RequestParam(value = "authorId") Long authorId) {

        return ResponseEntity.status(HttpStatus.OK).body(((ProductService) service).addAuthor(bookId, authorId));
    }
}
