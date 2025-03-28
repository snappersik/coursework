package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.dto.EventApplicationDTO;
import com.almetpt.coursework.bookclub.mapper.EventApplicationMapper;
import com.almetpt.coursework.bookclub.model.ApplicationStatus;
import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import com.almetpt.coursework.bookclub.repository.EventRepository;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventApplicationService extends GenericService<EventApplication, EventApplicationDTO> {

    private final EventApplicationRepository eventApplicationRepository;
    private final JavaMailSender javaMailSender;

    public EventApplicationService(
            EventApplicationRepository eventApplicationRepository,
            EventRepository eventRepository,
            JavaMailSender javaMailSender,
            EventApplicationMapper eventApplicationMapper) {
        super(eventApplicationRepository, eventApplicationMapper);
        this.eventApplicationRepository = eventApplicationRepository;
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public EventApplication createApplication(User user, Event event) {
        EventApplication application = new EventApplication();
        application.setUser(user);
        application.setEvent(event);

        int approvedCount = eventApplicationRepository.countApprovedApplicationsForEvent(event.getId());
        if (approvedCount < event.getMaxParticipants()) {
            application.setStatus(ApplicationStatus.APPROVED);
            SimpleMailMessage message = MailUtils.createMailMessage(
                    user.getEmail(),
                    "Заявка одобрена",
                    "Ваша заявка на мероприятие '" + event.getTitle() + "' одобрена."
            );
            javaMailSender.send(message);
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setRejectionReason(EventApplication.RejectionReason.NO_CAPACITY);
        }

        return eventApplicationRepository.save(application);
    }

    @Transactional
    public void markAsAttended(String qrCode) {
        EventApplication application = eventApplicationRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Invalid QR code"));

        application.setAttended(true);
        eventApplicationRepository.save(application);
    }
}
