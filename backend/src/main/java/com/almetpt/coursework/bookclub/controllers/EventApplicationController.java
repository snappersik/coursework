package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import com.almetpt.coursework.bookclub.service.EventApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest/event-applications")
@Tag(name = "Заявки на события", description = "Контроллер для работы с заявками на события")
public class EventApplicationController extends GenericController<EventApplication, EventApplicationDTO> {

    private final EventApplicationService eventApplicationService;
    private final UserRepository userRepository;

    public EventApplicationController(EventApplicationService eventApplicationService,
                                     UserRepository userRepository) {
        super(eventApplicationService);
        this.eventApplicationService = eventApplicationService;
        this.userRepository = userRepository;
    }

    @Override
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventApplicationDTO> create(@RequestBody EventApplicationDTO dto) {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(currentUserEmail);
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Устанавливаем ID текущего пользователя
        dto.setUserId(currentUser.getId());
        
        // Создаем заявку
        return ResponseEntity.ok(eventApplicationService.create(dto));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventApplicationDTO>> getCurrentUserApplications() {
        List<EventApplicationDTO> applications = eventApplicationService.getCurrentUserApplications();
        return ResponseEntity.ok(applications);
    }
}
