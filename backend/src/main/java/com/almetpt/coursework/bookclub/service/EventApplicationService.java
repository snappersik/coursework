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

import java.util.List;
import java.util.stream.Collectors;

@Service
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
            EventApplicationMapper eventApplicationMapper) {
        super(eventApplicationRepository, eventApplicationMapper);
        this.eventApplicationRepository = eventApplicationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    @Transactional
    public EventApplicationDTO create(EventApplicationDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + dto.getEventId()));

        EventApplication application = new EventApplication();
        application.setUser(user);
        application.setEvent(event);

        int approvedCount = eventApplicationRepository.countApprovedApplicationsForEvent(event.getId());
        if (approvedCount < event.getMaxParticipants()) {
            application.setStatus(ApplicationStatus.APPROVED);
            SimpleMailMessage message = MailUtils.createMailMessage(
                    user.getEmail(),
                    "Заявка одобрена",
                    "Ваша заявка на мероприятие '" + event.getTitle() + "' одобрена.");
            javaMailSender.send(message);
        } else {
            application.setStatus(ApplicationStatus.REJECTED);
            application.setRejectionReason(EventApplication.RejectionReason.NO_CAPACITY);
        }

        application = eventApplicationRepository.save(application);
        return mapper.toDTO(application);
    }

    @Transactional(readOnly = true)
    public List<EventApplicationDTO> getCurrentUserApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Пользователь не аутентифицирован");
        }

        String userEmail = authentication.getName();
        User user = userRepository.findUserByEmailAndIsDeletedFalse(userEmail);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        // Используем правильный метод репозитория
        List<EventApplication> applications = eventApplicationRepository.findByUser_IdAndIsDeletedFalse(user.getId());
        return applications.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

}
