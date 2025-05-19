package com.almetpt.coursework.bookclub.config;

import com.almetpt.coursework.bookclub.service.UserService;
import com.almetpt.coursework.bookclub.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MailScheduler {

    private final UserService userService;
    private final JavaMailSender javaMailSender;

    public MailScheduler(UserService userService, JavaMailSender javaMailSender) {
        this.userService = userService;
        this.javaMailSender = javaMailSender;
    }

    // Крон на каждую минуту: "0 0/1 * 1/1 * *"
    // Каждый день в 6 утра: "0 0 6 * * ?"
    @Scheduled(cron = "0 0 6 * * ?")
    public void sentMailsToDebtors() {
        log.info("Запуск планировщика по проверки должников…");
        List<String> emails = userService.getUserEmailsWithDelayedRentDate();

        if (!emails.isEmpty()) {
            SimpleMailMessage simpleMailMessage = MailUtils.createMailMessage(
                    emails,
                    "Напоминание о просрочке возврата книг(и)",
                    "Вы - злостный нарушитель!!! Верните книгу!!"
            );
            javaMailSender.send(simpleMailMessage);
        }
    }
}

