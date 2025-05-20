package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.service.EventApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Override
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventApplicationDTO> create(@RequestBody EventApplicationDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventApplicationDTO>> getCurrentUserApplications() {
        List<EventApplicationDTO> applications = eventApplicationService.getCurrentUserApplications();
        return ResponseEntity.ok(applications);
    }

}
