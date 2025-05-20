package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.repository.BookRepository;
import com.almetpt.coursework.bookclub.repository.EventRepository;
import com.almetpt.coursework.bookclub.repository.OrderRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import com.almetpt.coursework.bookclub.model.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rest/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;

    public AdminStatsController(UserRepository userRepository, 
                               BookRepository bookRepository, 
                               OrderRepository orderRepository,
                               EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGeneralStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", userRepository.countByIsDeletedFalse());
        stats.put("books", bookRepository.countByIsDeletedFalse());
        stats.put("orders", orderRepository.countByIsDeletedFalse());
        stats.put("events", eventRepository.countByIsDeletedFalse());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/users-chart")
    public ResponseEntity<Map<String, Object>> getUsersChart() {
        // Здесь должна быть логика для получения данных о регистрациях пользователей
        // Пример заглушки:
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", Arrays.asList("Янв", "Фев", "Мар", "Апр", "Май"));
        chartData.put("data", Arrays.asList(5, 8, 12, 7, 10));
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/stats/orders-chart")
    public ResponseEntity<Map<String, Object>> getOrdersChart() {
        // Здесь должна быть логика для получения данных о заказах
        // Пример заглушки:
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", Arrays.asList("Янв", "Фев", "Мар", "Апр", "Май"));
        chartData.put("data", Arrays.asList(10, 15, 8, 12, 20));
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/stats/events")
    public ResponseEntity<List<Map<String, Object>>> getEventStats() {
        try {
            List<Event> events = eventRepository.findByIsDeletedFalseAndIsCancelledFalse();
            
            List<Map<String, Object>> eventStats = events.stream()
                .map(event -> {
                    Map<String, Object> eventStat = new HashMap<>();
                    eventStat.put("id", event.getId());
                    eventStat.put("title", event.getTitle());
                    eventStat.put("eventType", event.getEventType());
                    eventStat.put("date", event.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                    eventStat.put("maxParticipants", event.getMaxParticipants());
                    
                    int approvedApplications = eventRepository.countApprovedApplications(event.getId());
                    eventStat.put("approvedApplications", approvedApplications);
                    
                    double fillPercentage = event.getMaxParticipants() > 0 
                        ? (approvedApplications * 100.0) / event.getMaxParticipants() 
                        : 0;
                    eventStat.put("fillPercentage", fillPercentage);
                    
                    return eventStat;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(eventStats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
