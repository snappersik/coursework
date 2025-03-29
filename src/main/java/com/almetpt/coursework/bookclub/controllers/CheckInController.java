package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {
    private final EventApplicationRepository eventApplicationRepository;

    @PostMapping("/{qrCodeData}")
    public ResponseEntity<?> processCheckIn(@PathVariable String qrCodeData) {
        EventApplication application = eventApplicationRepository.findByQrCode(qrCodeData)
                .orElseThrow(() -> new NotFoundException("Неверный QR-код"));

        // Проверка, что мероприятие еще не прошло
        if (application.getEvent().getDate().isBefore(LocalDateTime.now().minusDays(1))) {
            return ResponseEntity.badRequest().body("Мероприятие уже завершилось более 24 часов назад");
        }

        application.setAttended(true);
        eventApplicationRepository.save(application);

        return ResponseEntity.ok().body("Пользователь " + application.getUser().getLastName() +
                " отмечен как посетивший мероприятие " +
                application.getEvent().getTitle());
    }
}

