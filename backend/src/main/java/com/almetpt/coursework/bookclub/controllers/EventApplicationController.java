package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import com.almetpt.coursework.bookclub.service.EventApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/rest/event-applications")
@Tag(name = "Заявки на события", description = "Контроллер для работы с заявками на события")
@Slf4j
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
        log.info("EventApplicationController: Received request to create event application with DTO: {}", dto);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("EventApplicationController: User not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(currentUserEmail);
        if (currentUser == null) {
            log.warn("EventApplicationController: Authenticated user with email {} not found in repository.",
                    currentUserEmail);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            // Возможно, стоит вернуть более конкретную ошибку
        }
        log.debug("EventApplicationController: Current user ID: {}, Email: {}", currentUser.getId(), currentUserEmail);

        dto.setUserId(currentUser.getId());
        log.debug("EventApplicationController: Set userId in DTO to: {}", currentUser.getId());

        try {
            log.debug("EventApplicationController: Calling eventApplicationService.create()...");
            EventApplicationDTO createdApplication = eventApplicationService.create(dto);
            log.info("EventApplicationController: Successfully created event application, ID: {}",
                    createdApplication.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdApplication);
        } catch (IllegalArgumentException e) {
            log.error(
                    "EventApplicationController: IllegalArgumentException during event application creation: {}. DTO: {}",
                    e.getMessage(), dto, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Рассмотрите возможность возврата объекта
                                                                             // ошибки
        } catch (IllegalStateException e) {
            log.error(
                    "EventApplicationController: IllegalStateException during event application creation: {}. DTO: {}",
                    e.getMessage(), dto, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // Рассмотрите возможность возврата объекта
                                                                          // ошибки
        } catch (Exception e) {
            log.error(
                    "EventApplicationController: Generic exception during event application creation for DTO: {}. Error: {}",
                    dto, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Рассмотрите возможность
                                                                                       // возврата объекта ошибки
        }
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventApplicationDTO>> getCurrentUserApplications() {
        log.info("EventApplicationController: Received request to get current user applications.");
        try {
            List<EventApplicationDTO> applications = eventApplicationService.getCurrentUserApplications();
            log.info("EventApplicationController: Successfully fetched {} applications for current user.",
                    applications.size());
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("EventApplicationController: Error fetching current user applications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Получить страницу заявок на мероприятия", description = "Возвращает страницу заявок на мероприятия с пагинацией. Доступно администраторам.")
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Page<EventApplicationDTO>> getEventApplicationsPaginated(
            @PageableDefault(size = 20, sort = "createdWhen") Pageable pageable) {
        log.debug("EventApplicationController: Fetching paginated event applications for admin. Pageable: {}",
                pageable);
        // Если админ должен видеть ВСЕ заявки (включая удаленные), используйте
        // service.listAll(pageable)
        // Если только активные (не удаленные), то service.listAllNotDeleted(pageable)
        // Для админской панели часто имеет смысл показывать все или иметь фильтр.
        // Начнем с неудаленных.
        Page<EventApplicationDTO> applicationsPage = eventApplicationService.listAllNotDeleted(pageable);
        log.info("EventApplicationController: Fetched {} paginated event applications.",
                applicationsPage.getTotalElements());
        return ResponseEntity.ok(applicationsPage);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelEventApplication(@PathVariable Long id) {
        log.info("EventApplicationController: Received request to cancel application with id: {}", id);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("EventApplicationController: User not authenticated.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не аутентифицирован");
            }

            String currentUserEmail = authentication.getName();
            User currentUser = userRepository.findUserByEmailAndIsDeletedFalse(currentUserEmail);
            if (currentUser == null) {
                log.warn("EventApplicationController: Authenticated user with email {} not found in repository.",
                        currentUserEmail);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Пользователь не найден");
            }

            boolean success = eventApplicationService.cancelByUser(id, currentUser.getId());
            if (success) {
                log.info("EventApplicationController: Application {} cancelled by user {}", id, currentUser.getId());
                return ResponseEntity.ok().body("Заявка успешно отменена");
            } else {
                log.warn("EventApplicationController: Application {} not found or not owned by user {}", id,
                        currentUser.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Заявка не найдена или не принадлежит пользователю");
            }
        } catch (Exception e) {
            log.error("EventApplicationController: Error cancelling application: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при отмене заявки");
        }
    }

}
