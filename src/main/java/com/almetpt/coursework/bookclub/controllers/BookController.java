package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.dto.BookImageDTO;
import com.almetpt.coursework.bookclub.dto.BookImageUploadDTO;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.service.BookImageService;
import com.almetpt.coursework.bookclub.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/rest/books")
@Tag(name = "Книги", description = "Контроллер для работы с книгами")
public class BookController extends GenericController<Book, BookDTO> {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;
    private final BookImageService bookImageService;

    public BookController(BookService bookService, BookImageService bookImageService) {
        super(bookService);
        this.bookService = bookService;
        this.bookImageService = bookImageService;
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
            BookDTO book = bookService.getOne(id);
            
            // Если нет данных об обложке
            if (book == null || (book.getCoverImageUrl() == null && book.getCoverImageFilename() == null)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageData;
            
            // Если есть внешний URL - загружаем через прокси
            if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
                try {
                    log.info("Загрузка изображения с внешнего URL: {}", book.getCoverImageUrl());
                    imageData = downloadExternalImage(book.getCoverImageUrl());
                    
                    // Опционально: сохраняем загруженное изображение для будущего использования
                    bookImageService.saveDownloadedImage(id, imageData);
                    
                    return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                        .body(imageData);
                } catch (Exception e) {
                    log.error("Ошибка загрузки внешнего изображения: {}", book.getCoverImageUrl(), e);
                    
                    // Если есть локальный файл - используем его как запасной вариант
                    if (book.getCoverImageFilename() != null) {
                        imageData = bookImageService.getBookCoverImageData(id);
                        return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(determineContentType(book.getCoverImageFilename())))
                            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                            .body(imageData);
                    }
                    
                    // Если нет локального файла - возвращаем 404
                    return ResponseEntity.notFound().build();
                }
            }
            
            // Если есть локальный файл
            if (book.getCoverImageFilename() != null) {
                imageData = bookImageService.getBookCoverImageData(id);
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(determineContentType(book.getCoverImageFilename())))
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                    .body(imageData);
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ошибка при получении обложки книги с id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private byte[] downloadExternalImage(String url) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        try {
            log.info("Загрузка изображения с URL: {}", url);
            byte[] imageData = restTemplate.getForObject(url, byte[].class);
            log.info("Изображение успешно загружено, размер: {} байт", imageData != null ? imageData.length : 0);
            return imageData;
        } catch (RestClientException e) {
            log.error("Не удалось загрузить изображение с URL: {}", url, e);
            throw e;
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
