package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.dto.BookImageDTO;
import com.almetpt.coursework.bookclub.dto.BookImageUploadDTO;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.model.BookGenre;
import com.almetpt.coursework.bookclub.service.BookImageService;
import com.almetpt.coursework.bookclub.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;

import java.io.IOException;
import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rest/books")
@Tag(name = "Книги", description = "Контроллер для работы с книгами")
public class BookController extends GenericController<Book, BookDTO> {

    private final BookService bookService;
    private final BookImageService bookImageService;

    public BookController(BookService bookService, BookImageService bookImageService) {
        super(bookService);
        this.bookService = bookService;
        this.bookImageService = bookImageService;
    }

    @Operation(summary = "Добавить жанр книге", description = "Добавляет жанр к существующей книге")
    @PostMapping("/{id}/genres")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<BookDTO> addGenre(
            @Parameter(description = "ID книги") @PathVariable Long id,
            @Parameter(description = "Жанр книги") @RequestBody BookGenre genre) {
        return ResponseEntity.ok(bookService.addGenre(id, genre));
    }

    @Operation(summary = "Удалить жанр у книги", description = "Удаляет жанр у существующей книги")
    @DeleteMapping("/{id}/genres/{genre}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<BookDTO> removeGenre(
            @Parameter(description = "ID книги") @PathVariable Long id,
            @Parameter(description = "Жанр книги") @PathVariable BookGenre genre) {
        return ResponseEntity.ok(bookService.removeGenre(id, genre));
    }

    @Operation(summary = "Установить жанры книге", description = "Устанавливает список жанров для книги")
    @PutMapping("/{id}/genres")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<BookDTO> setGenres(
            @Parameter(description = "ID книги") @PathVariable Long id,
            @Parameter(description = "Список жанров книги") @RequestBody Set<BookGenre> genres) {
        return ResponseEntity.ok(bookService.setGenres(id, genres));
    }

    @Operation(summary = "Получить страницу книг", description = "Возвращает страницу книг с пагинацией и сортировкой")
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

    @Operation(summary = "Загрузить изображение обложки книги", description = "Загружает изображение обложки для указанной книги в виде файла")
    @PostMapping(path = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookImageDTO> uploadBookCover(
            @Parameter(description = "ID книги") @PathVariable Long id,
            @Parameter(description = "Файл изображения", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary"))) @RequestPart("file") MultipartFile file) {
        try {
            BookImageDTO result = bookImageService.uploadImage(id, file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Загрузить изображение обложки книги из URL", description = "Устанавливает URL внешнего изображения в качестве обложки книги")
    @PostMapping("/url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookImageDTO> setBookCoverUrl(
            @Parameter(description = "Информация об изображении") @RequestBody BookImageUploadDTO dto) {
        BookImageDTO result = bookImageService.uploadImageFromUrl(dto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Загрузить бинарные данные изображения книги", description = "Загружает и сохраняет бинарные данные изображения обложки книги")
    @PostMapping(path = "/{id}/upload-binary", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookImageDTO> uploadBookCoverBinary(
            @Parameter(description = "ID книги") @PathVariable Long id,
            @Parameter(description = "Файл изображения", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary"))) @RequestPart("file") MultipartFile file) {
        try {
            BookImageDTO result = bookImageService.uploadImageData(id, file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить информацию об изображении книги", description = "Возвращает информацию об изображении обложки книги")
    @GetMapping("/{id}/image-info")
    public ResponseEntity<BookImageDTO> getBookImageInfo(
            @Parameter(description = "ID книги") @PathVariable Long id) {
        BookImageDTO result = bookImageService.getBookImageInfo(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/cover")
    public ResponseEntity<byte[]> getBookCover(@PathVariable Long id) {
        try {
            // Получаем информацию о книге
            BookDTO book = bookService.getOne(id);

            // Если есть URL, делаем редирект
            if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(book.getCoverImageUrl()))
                        .build();
            }

            // Если есть локальный файл
            if (book.getCoverImageFilename() != null) {
                // Получаем данные файла
                byte[] imageData = bookImageService.getBookCoverImageData(id);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(determineContentType(book.getCoverImageFilename())))
                        .body(imageData);
            }

            // Если нет ни URL, ни файла
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Определение типа контента по расширению файла
    private String determineContentType(String filename) {
        if (filename.endsWith(".webp"))
            return "image/webp";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
            return "image/jpeg";
        if (filename.endsWith(".png"))
            return "image/png";
        return "application/octet-stream";
    }

}
