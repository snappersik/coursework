package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.dto.ContactDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/contact")
public class ContactController {

    @Autowired
    private JavaMailSender mailSender;

    // Замените на почту администратора
    private static final String ADMIN_EMAIL = "admin@example.com";

    @PostMapping
    public void sendContactMessage(@RequestBody ContactDTO contactDTO) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(ADMIN_EMAIL);
        message.setSubject("Новое сообщение с формы обратной связи");
        message.setText(
            "Имя: " + contactDTO.getName() + "\n" +
            "Email: " + contactDTO.getFromEmail() + "\n" +
            "Телефон: " + contactDTO.getPhone() + "\n" +
            "Сообщение:\n" + contactDTO.getBody()
        );
        mailSender.send(message);
    }
}
