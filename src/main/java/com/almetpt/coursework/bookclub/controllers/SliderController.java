package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.BookDTO;
import com.almetpt.coursework.bookclub.dto.SliderBookDTO;
import com.almetpt.coursework.bookclub.service.SliderBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest/slider")
@Tag(name = "Слайдер", description = "Контроллер для управления слайдером на главной странице")
public class SliderController {

    private final SliderBookService sliderBookService;

    public SliderController(SliderBookService sliderBookService) {
        this.sliderBookService = sliderBookService;
    }

    @Operation(summary = "Получить книги для слайдера", description = "Возвращает книги для слайдера или подходящую замену")
    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> getSliderBooks() {
        return ResponseEntity.ok(sliderBookService.getSliderBooksOrAlternative());
    }

    @Operation(summary = "Получить все книги слайдера", description = "Возвращает все добавленные в слайдер книги")
    @GetMapping("/admin/books")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<SliderBookDTO>> getAllSliderBooks() {
        return ResponseEntity.ok(sliderBookService.getAllSliderBooks());
    }

    @Operation(summary = "Получить книгу слайдера по ID", description = "Возвращает информацию о книге в слайдере по её ID")
    @GetMapping("/admin/books/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SliderBookDTO> getSliderBook(@Parameter(description = "ID книги в слайдере") @PathVariable Long id) {
        return ResponseEntity.ok(sliderBookService.getOne(id));
    }

    @Operation(summary = "Добавить книгу в слайдер", description = "Добавляет книгу в слайдер с опциональным кастомным описанием")
    @PostMapping("/admin/books")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SliderBookDTO> createSliderBook(@RequestBody SliderBookDTO sliderBookDTO) {
        return ResponseEntity.ok(sliderBookService.create(sliderBookDTO));
    }

    @Operation(summary = "Обновить книгу в слайдере", description = "Обновляет информацию о книге в слайдере")
    @PutMapping("/admin/books/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SliderBookDTO> updateSliderBook(
            @Parameter(description = "ID книги в слайдере") @PathVariable Long id,
            @RequestBody SliderBookDTO sliderBookDTO) {
        sliderBookDTO.setId(id);
        return ResponseEntity.ok(sliderBookService.update(sliderBookDTO));
    }

    @Operation(summary = "Удалить книгу из слайдера", description = "Удаляет книгу из слайдера")
    @DeleteMapping("/admin/books/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteSliderBook(@Parameter(description = "ID книги в слайдере") @PathVariable Long id) {
        sliderBookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
