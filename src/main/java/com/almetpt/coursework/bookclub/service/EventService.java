package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.EventDTO;
import com.almetpt.coursework.bookclub.mapper.EventMapper;
import com.almetpt.coursework.bookclub.model.ApplicationStatus;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import com.almetpt.coursework.bookclub.repository.EventRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class EventService extends GenericService<Event, EventDTO> {

    private final EventRepository eventRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final JavaMailSender javaMailSender;

    public EventService(EventRepository eventRepository,
                        EventApplicationRepository eventApplicationRepository,
                        JavaMailSender javaMailSender,
                        EventMapper eventMapper) {
        super(eventRepository, eventMapper);
        this.eventRepository = eventRepository;
        this.eventApplicationRepository = eventApplicationRepository;
        this.javaMailSender = javaMailSender;
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
                    "Мероприятие '" + event.getTitle() + "' отменено. Причина: " + cancellationReason
            );
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

        LocalDateTime oldDate = event.getDate();
        event.setDate(newDate);
        eventRepository.save(event);

        List<EventApplication> applications = eventApplicationRepository.findApprovedApplicationsForEvent(eventId);
        applications.forEach(app -> {
            SimpleMailMessage message = MailUtils.createMailMessage(
                    app.getUser().getEmail(),
                    "Мероприятие перенесено",
                    "Мероприятие '" + event.getTitle() + "' перенесено. Новая дата: " + newDate + ". Причина: " + rescheduleMessage
            );
            javaMailSender.send(message);
        });
    }
}
