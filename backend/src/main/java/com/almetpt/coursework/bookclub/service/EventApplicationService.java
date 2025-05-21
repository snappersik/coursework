package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;
import com.almetpt.coursework.bookclub.mapper.EventApplicationMapper;
import com.almetpt.coursework.bookclub.model.ApplicationStatus;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import com.almetpt.coursework.bookclub.repository.EventRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventApplicationService extends GenericService<EventApplication, EventApplicationDTO> {

    private final EventApplicationRepository eventApplicationRepository;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventApplicationService(
            EventApplicationRepository eventApplicationRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            JavaMailSender javaMailSender,
            EventApplicationMapper eventApplicationMapper) { // Убедитесь, что сюда передается именно EventApplicationMapper
        super(eventApplicationRepository, eventApplicationMapper); // Он сохранится в protected final GenericMapper<E, D> mapper;
        this.eventApplicationRepository = eventApplicationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public EventApplicationDTO create(EventApplicationDTO dto) {
        log.info("Starting creation of event application for userId: {} and eventId: {}", dto.getUserId(), dto.getEventId());

        if (dto.getUserId() == null || dto.getEventId() == null) {
            log.error("UserId or EventId is null in DTO: {}", dto);
            throw new IllegalArgumentException("UserId и EventId не могут быть null");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", dto.getUserId());
                    return new NotFoundException("User not found with id: " + dto.getUserId());
                });

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> {
                    log.error("Event not found with id: {}", dto.getEventId());
                    return new NotFoundException("Event not found with id: " + dto.getEventId());
                });

        List<EventApplication> existingApplications = eventApplicationRepository
                .findByUser_IdAndEvent_IdAndIsDeletedFalse(user.getId(), event.getId());
        if (!existingApplications.isEmpty()) {
            log.warn("User {} already applied for event {}", user.getId(), event.getId());
            throw new IllegalStateException("Вы уже подали заявку на это мероприятие");
        }

        EventApplication application = new EventApplication();
        application.setUser(user);
        application.setEvent(event);

        int approvedCount = eventApplicationRepository.countApprovedApplicationsForEvent(event.getId());
        log.debug("Approved applications count for event {}: {}", event.getId(), approvedCount);

        if (approvedCount < event.getMaxParticipants()) {
            application.setStatus(ApplicationStatus.APPROVED);
            try {
                SimpleMailMessage message = MailUtils.createMailMessage(
                        user.getEmail(),
                        "Заявка одобрена",
                        "Ваша заявка на мероприятие '" + event.getTitle() + "' одобрена.");
                javaMailSender.send(message);
                log.info("Email sent successfully to user: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage(), e);
                // Не прерываем выполнение
            }
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setRejectionReason(EventApplication.RejectionReason.NO_CAPACITY);
            log.info("Application rejected due to no capacity for event: {}", event.getId());
        }

        setAuditFields(application, true); // Устанавливает createdBy, createdWhen из GenericService

        EventApplication savedApplication;
        try {
            // Явно сохраняем перед тем, как использовать ID или другие поля
            savedApplication = eventApplicationRepository.saveAndFlush(application); // Используем saveAndFlush для немедленной записи в БД
            log.info("Event application entity saved and flushed successfully with id: {}", savedApplication.getId());
        } catch (Exception e) {
            log.error("CRITICAL: Error during eventApplicationRepository.saveAndFlush(). Application before save: userId={}, eventId={}. Exception: {}",
                    application.getUser() != null ? application.getUser().getId() : "null",
                    application.getEvent() != null ? application.getEvent().getId() : "null",
                    e.getMessage(), e);
            throw e; // Откат транзакции
        }


        EventApplicationDTO applicationDTO;
        try {
            log.debug("Attempting to map EventApplication (id: {}) to DTO...", savedApplication.getId());
            // 'this.mapper' - это экземпляр EventApplicationMapper, переданный в конструктор GenericService
            applicationDTO = this.mapper.toDTO(savedApplication);
            if (applicationDTO == null) {
                log.error("CRITICAL: this.mapper.toDTO(application) returned null for application id: {}", savedApplication.getId());
                throw new IllegalStateException("Failed to map EventApplication to DTO: mapper returned null. Application ID: " + savedApplication.getId());
            }
            // Полное DTO логировать опасно, если оно содержит чувствительные данные или сложную структуру.
            // Логируем ключевые идентификаторы.
            log.info("Mapped event application (id: {}) to DTO successfully. DTO_Id: {}, DTO_UserId: {}, DTO_EventId: {}, DTO_Status: {}",
                    savedApplication.getId(), applicationDTO.getId(), applicationDTO.getUserId(), applicationDTO.getEventId(), applicationDTO.getApplicationStatus());
        } catch (Exception e) {
            log.error("CRITICAL: Error during EventApplication (id: {}) mapping to DTO. Saved Application Details: User ID={}, Event ID={}, Status={}. Exception: {}",
                    savedApplication.getId(),
                    savedApplication.getUser() != null ? savedApplication.getUser().getId() : "null",
                    savedApplication.getEvent() != null ? savedApplication.getEvent().getId() : "null",
                    savedApplication.getStatus(),
                    e.getMessage(), e);
            throw e; // Перебрасываем исключение, чтобы гарантировать откат транзакции
        }

        return applicationDTO;
    }

    @Transactional(readOnly = true)
    public List<EventApplicationDTO> getCurrentUserApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User is not authenticated");
            throw new AccessDeniedException("Пользователь не аутентифицирован");
        }

        String userEmail = authentication.getName();
        User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
        if (user == null) {
            log.error("User not found by email: {}", userEmail);
            throw new NotFoundException("Пользователь не найден");
        }

        List<EventApplication> applications = eventApplicationRepository.findByUser_IdAndIsDeletedFalse(user.getId());
        log.info("Found {} applications for user {}", applications.size(), user.getId());

        // Убедимся, что здесь также используется корректный маппер
        return applications.stream()
                .map(this.mapper::toDTO) // Явно используем this.mapper
                .collect(Collectors.toList());
    }
}

