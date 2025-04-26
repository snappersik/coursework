package com.almetpt.coursework.bookclub.service;

import com.almetpt.coursework.bookclub.model.Event;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import com.almetpt.coursework.bookclub.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EventAttendanceScheduler {

    private final EventApplicationRepository eventApplicationRepository;
    private final EventRepository eventRepository;

    public EventAttendanceScheduler(EventApplicationRepository eventApplicationRepository,
                                    EventRepository eventRepository) {
        this.eventApplicationRepository = eventApplicationRepository;
        this.eventRepository = eventRepository;
    }

    // Запускается каждый день в полночь
    @Scheduled(cron = "0 0 0 * * *")
    public void markMissedEvents() {
        log.info("Запуск проверки непосещенных мероприятий");

        // Находим все мероприятия, которые прошли более 24 часов назад
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);
        List<Event> pastEvents = eventRepository.findEventsBeforeDate(cutoffTime);

        for (Event event : pastEvents) {
            // Получаем все заявки на это мероприятие со статусом APPROVED, которые не отмечены как посещенные
            List<EventApplication> applications = eventApplicationRepository
                    .findApprovedNotAttendedApplicationsForEvent(event.getId());

            for (EventApplication application : applications) {
                // Отмечаем как непосещенные
                application.setAttended(false);
                eventApplicationRepository.save(application);
                log.info("Мероприятие {} отмечено как непосещенное для пользователя {}",
                        event.getTitle(), application.getUser().getEmail());
            }
        }
    }
}

