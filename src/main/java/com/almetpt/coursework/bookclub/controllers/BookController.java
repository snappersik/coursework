package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/books")
@Tag(name = "Книги", description = "Контроллер для работы с книгами")
public class BookController extends GenericController<Book, BookDTO> {

    private final BookService bookService;

    public BookController(BookService bookService) {
        super(bookService);
        this.bookService = bookService;
    }

    @Operation(
        summary = "Получить страницу книг",
        description = "Возвращает страницу книг с пагинацией и сортировкой"
    )
    @GetMapping("/paginated")
    public ResponseEntity<Page<BookDTO>> getBooksPaginated(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "Направление сортировки (ASC или DESC)") @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<BookDTO> books = bookService.listAll(pageRequest);
        return ResponseEntity.ok(books);
    }
}
