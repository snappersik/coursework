package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@Tag(name = "Регистрация на мероприятиях", description = "Контроллер для регистрации посещений мероприятий")
public class CheckInController {

    private final EventApplicationRepository eventApplicationRepository;

    @Operation(
        summary = "Зарегистрировать посещение",
        description = "Отмечает посещение мероприятия по QR-коду заявки"
    )
    @PostMapping("/{qrCodeData}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @AdminAction
    public ResponseEntity<?> processCheckIn(
            @Parameter(description = "Данные QR-кода") @PathVariable String qrCodeData) {
        try {
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
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Произошла ошибка при регистрации посещения: " + e.getMessage());
        }
    }
}
