package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.service.EventApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/event-applications")
@Tag(name = "Заявки на события", description = "Контроллер для работы с заявками на события")
public class EventApplicationController extends GenericController<EventApplication, EventApplicationDTO> {

    private final EventApplicationService eventApplicationService;

    public EventApplicationController(EventApplicationService eventApplicationService) {
        super(eventApplicationService);
        this.eventApplicationService = eventApplicationService;
    }

    @PostMapping
    public ResponseEntity<EventApplicationDTO> createEventApplication(@RequestBody EventApplicationDTO dto) {
        EventApplicationDTO created = eventApplicationService.createApplicationFromDTO(dto);
        return ResponseEntity.ok(created);
    }





    @PostMapping("/mark-attended/{qrCode}")
    public ResponseEntity<Void> markAsAttended(@PathVariable String qrCode) {
        eventApplicationService.markAsAttended(qrCode);
        return ResponseEntity.ok().build();
    }
}
