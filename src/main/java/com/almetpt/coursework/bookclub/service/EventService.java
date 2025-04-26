package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.constants.Errors;
import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.exception.MyDeleteException;
import com.almetpt.coursework.bookclub.mapper.EventMapper;
import com.almetpt.coursework.bookclub.model.ApplicationStatus;
import com.almetpt.coursework.bookclub.model.Book;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.repository.BookRepository;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import com.almetpt.coursework.bookclub.repository.EventRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final BookRepository bookRepository;

    public EventService(EventRepository eventRepository,
            EventApplicationRepository eventApplicationRepository,
            BookRepository bookRepository,
            JavaMailSender javaMailSender,
            EventMapper eventMapper) {
        super(eventRepository, eventMapper);
        this.eventRepository = eventRepository;
        this.eventApplicationRepository = eventApplicationRepository;
        this.bookRepository = bookRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    @Transactional
    public EventDTO create(EventDTO dto) {
        Event event = (Event) mapper.toEntity(dto);

        // Установка времени создания
        event.setCreatedWhen(LocalDateTime.now());

        // Если указан ID книги, устанавливаем связь с книгой
        if (dto.getBookId() != null) {
            Book book = bookRepository.findById(dto.getBookId())
                    .orElseThrow(() -> new NotFoundException("Book not found with id: " + dto.getBookId()));
            event.setBook(book);
        }

        // Сохраняем мероприятие
        event = eventRepository.save(event);

        // Преобразуем обратно в DTO и возвращаем
        return (EventDTO) mapper.toDTO(event);
    }

    @Transactional
    public void cancelEvent(Long eventId, String cancellationReason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        event.setCancelled(true);
        event.setCancellationReason(cancellationReason);
        eventRepository.save(event);

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
        // LocalDateTime oldDate = event.getDate();
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
