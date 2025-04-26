package com.almetpt.coursework;

import com.almetpt.coursework.bookclub.model.Role;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.repository.RoleRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@EnableAspectJAutoProxy
public class CourseworkApplication {

    @Value("${server.port}")
    private String serverPort;

    public static void main(String[] args) {
        SpringApplication.run(CourseworkApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Инициализация ролей
            Role userRole = null;
            Role organizerRole = null;
            Role adminRole = null;
            
            if (roleRepository.count() == 0) {
                userRole = new Role();
                userRole.setName("USER");
                userRole = roleRepository.save(userRole);
                
                organizerRole = new Role();
                organizerRole.setName("ORGANIZER");
                organizerRole = roleRepository.save(organizerRole);
                
                adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole = roleRepository.save(adminRole);
                
                log.info("Роли успешно созданы");
            } else {
                // Получаем существующие роли, если они уже созданы
                userRole = roleRepository.findByName("USER").orElse(null);
                organizerRole = roleRepository.findByName("ORGANIZER").orElse(null);
                adminRole = roleRepository.findByName("ADMIN").orElse(null);
            }
            
            // Создание администратора только после создания ролей
            if (adminRole != null && userRepository.findByEmail("admin").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole(adminRole);
                admin.setCreatedWhen(LocalDateTime.now());
                admin.setBirthDate(LocalDate.of(2000, 1, 1));
                userRepository.save(admin);
                
                log.info("Администратор создан с email: admin и паролем: admin");
            } else if (userRepository.findByEmail("admin").isPresent()) {
                log.info("Администратор уже существует");
            }
        };
    }

    @Bean
    CommandLineRunner printApplicationUrl() {
        return args -> {
            log.info("Swagger UI: http://localhost:{}/swagger-ui/index.html", serverPort);
            log.info("Application: http://localhost:{}/", serverPort);
        };
    }
}
