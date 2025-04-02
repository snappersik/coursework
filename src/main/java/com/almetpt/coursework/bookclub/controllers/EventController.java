package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.service.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rest/events")
@Tag(name = "События", description = "Контроллер для работы с событиями")
public class EventController extends GenericController<Event, EventDTO> {

    private final EventService eventService;

    public EventController(EventService eventService) {
        super(eventService);
        this.eventService = eventService;
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<?> cancelEvent(@PathVariable Long eventId, @RequestParam String reason) {
        eventService.cancelEvent(eventId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{eventId}/reschedule")
    public ResponseEntity<?> rescheduleEvent(
            @PathVariable Long eventId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate,
            @RequestParam String reason) {
        eventService.rescheduleEvent(eventId, newDate, reason);
        return ResponseEntity.ok().build();
    }
}
