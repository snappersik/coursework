package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.RescheduleRequest;
import com.almetpt.coursework.bookclub.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
