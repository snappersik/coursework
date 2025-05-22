package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.RescheduleRequest;
import com.almetpt.coursework.bookclub.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/events")
@Tag(name = "События", description = "Контроллер для работы с событиями")
public class EventController extends GenericController<Event, EventDTO> {

    private final EventService eventService;

    public EventController(EventService eventService) {
        super(eventService);
        this.eventService = eventService;
    }

    // Новый метод для обработки пагинированного запроса
    @Operation(summary = "Получить список мероприятий с пагинацией", description = "Возвращает список неудаленных мероприятий с учетом пагинации и сортировки")
    @GetMapping("/paginated")
    public ResponseEntity<Page<EventDTO>> getPaginatedEvents(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "100") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки (ASC или DESC)") @RequestParam(defaultValue = "ASC") String direction) {
        // Создаем объект сортировки
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        // Создаем объект пагинации
        Pageable pageable = PageRequest.of(page, size, sort);
        // Получаем данные из сервиса
        Page<EventDTO> eventPage = eventService.listAllNotDeleted(pageable);
        // Возвращаем результат
        return ResponseEntity.ok(eventPage);
    }

    @Operation(summary = "Отменить мероприятие", description = "Позволяет отменить мероприятие с указанием причины отмены. Доступно администраторам и организаторам.")
    @PostMapping("/{eventId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @AdminAction
    public ResponseEntity<?> cancelEvent(
            @Parameter(description = "ID мероприятия") @PathVariable Long eventId,
            @Parameter(description = "Причина отмены") @RequestParam String reason) {
        eventService.cancelEvent(eventId, reason);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Перенести мероприятие", description = "Позволяет перенести мероприятие на другую дату с указанием причины переноса. Доступно администраторам и организаторам.")
    @PostMapping("/{eventId}/reschedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @AdminAction
    public ResponseEntity<?> rescheduleEvent(
            @Parameter(description = "ID мероприятия") @PathVariable Long eventId,
            @RequestBody RescheduleRequest request) {
        eventService.rescheduleEvent(eventId, request.getNewDate(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<EventDTO> create(@RequestBody EventDTO dto) {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        // Устанавливаем текущего пользователя как создателя
        dto.setCreatedBy(currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(dto));
    }
}
