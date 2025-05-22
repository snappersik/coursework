package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.Errors;
import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.exception.MyDeleteException;
import com.almetpt.coursework.bookclub.mapper.EventMapper;
import com.almetpt.coursework.bookclub.model.ApplicationStatus;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.repository.BookRepository;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import com.almetpt.coursework.bookclub.repository.EventRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class EventService extends GenericService<Event, EventDTO> {

    private final EventRepository eventRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final JavaMailSender javaMailSender;

    public EventService(EventRepository eventRepository,
                        EventApplicationRepository eventApplicationRepository,
                        BookRepository bookRepository,
                        JavaMailSender javaMailSender,
                        EventMapper eventMapper) {
        super(eventRepository, eventMapper);
        this.eventRepository = eventRepository;
        this.eventApplicationRepository = eventApplicationRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    @Transactional
    public EventDTO create(EventDTO dto) {
        // Проверка, что дата мероприятия не раньше, чем через день от текущей даты
        LocalDateTime minimumAllowedDate = LocalDateTime.now().plusDays(1);
        if (dto.getDate().isBefore(minimumAllowedDate)) {
            throw new IllegalArgumentException(
                    "Мероприятие не может быть создано раньше, чем через день от текущей даты");
        }

        Event event = mapper.toEntity(dto);
        event.setCreatedWhen(LocalDateTime.now());
        event = eventRepository.save(event);
        return mapper.toDTO(event);
    }

    @Transactional
    public void cancelEvent(Long eventId, String cancellationReason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        // Проверяем, является ли текущий пользователь администратором
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Если пользователь не админ, проверяем, является ли он создателем мероприятия
        if (!isAdmin && (event.getCreatedBy() == null || !event.getCreatedBy().equals(currentUserEmail))) {
            throw new AccessDeniedException("Вы можете отменять только созданные вами мероприятия");
        }

        log.debug("Cancelling event: {} with reason: {}", eventId, cancellationReason);
        log.debug("Before update - isCancelled: {}", event.isCancelled());
        
        event.setCancelled(true);
        event.setCancellationReason(cancellationReason);
        
        Event savedEvent = eventRepository.save(event);
        log.debug("After update - isCancelled: {}", savedEvent.isCancelled());
        
        List<EventApplication> applications = eventApplicationRepository.findApprovedApplicationsForEvent(eventId);
        applications.forEach(app -> {
            SimpleMailMessage message = MailUtils.createMailMessage(
                    app.getUser().getEmail(),
                    "Мероприятие отменено",
                    "Мероприятие '" + event.getTitle() + "' отменено. Причина: " + cancellationReason);
            javaMailSender.send(message);
            
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason(EventApplication.RejectionReason.EVENT_CANCELLED);
            eventApplicationRepository.save(app);
        });
    }

    @Transactional
    public void rescheduleEvent(Long eventId, LocalDateTime newDate, String rescheduleMessage) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        // Проверяем, является ли текущий пользователь администратором
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Если пользователь не админ, проверяем, является ли он создателем мероприятия
        if (!isAdmin && (event.getCreatedBy() == null || !event.getCreatedBy().equals(currentUserEmail))) {
            throw new AccessDeniedException("Вы можете переносить только созданные вами мероприятия");
        }

        // Проверка, что новая дата не раньше, чем через день от текущей даты
        LocalDateTime minimumAllowedDate = LocalDateTime.now().plusDays(1);
        if (newDate.isBefore(minimumAllowedDate)) {
            throw new IllegalArgumentException(
                    "Мероприятие не может быть перенесено на дату раньше, чем через день от текущей даты");
        }

        event.setDate(newDate);
        eventRepository.save(event);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        List<EventApplication> applications = eventApplicationRepository.findApprovedApplicationsForEvent(eventId);
        
        applications.forEach(app -> {
            SimpleMailMessage message = MailUtils.createMailMessage(
                    app.getUser().getEmail(),
                    "Мероприятие перенесено",
                    "Мероприятие '" + event.getTitle() + "' перенесено. Новая дата: " + newDate.format(formatter)
                            + ". Причина: " + rescheduleMessage);
            javaMailSender.send(message);
        });
    }

    @Override
    public void deleteSoft(final Long id) throws MyDeleteException {
        Event event = repository.findById(id).orElseThrow(
                () -> new NotFoundException("Мероприятие не найдено"));
        boolean eventCanBeDeleted = ((EventRepository) repository).isEventCanBeDeleted(id);
        if (eventCanBeDeleted) {
            markAsDeleted(event);
            repository.save(event);
        } else {
            throw new MyDeleteException(Errors.Events.EVENT_DELETED_ERROR);
        }
    }

    protected NotFoundException createNotFoundException(Long id) {
        return new NotFoundException(Errors.Events.EVENT_NOT_FOUND.formatted(id));
    }

    private void markAsDeleted(Event event) {
        event.setDeletedWhen(LocalDateTime.now());
        event.setDeleted(true);
    }
}
