package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.model.EventApplication;
import com.almetpt.coursework.bookclub.repository.EventApplicationRepository;
import com.google.zxing.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final EventApplicationRepository eventApplicationRepository;

    @PostMapping("/{qrCodeData}")
    public ResponseEntity<?> processCheckIn(@PathVariable String qrCodeData) {
        EventApplication application = eventApplicationRepository.findByQrCode(qrCodeData)
                .orElseThrow(() -> new NotFoundException("Invalid QR code"));

        application.setAttended(true);
        eventApplicationRepository.save(application);

        return ResponseEntity.ok().build();
    }
}
