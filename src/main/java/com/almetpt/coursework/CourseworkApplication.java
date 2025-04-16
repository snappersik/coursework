package com.almetpt.coursework;

import com.almetpt.coursework.bookclub.model.Role;
import com.almetpt.coursework.bookclub.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

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
    CommandLineRunner initDatabase(RoleRepository roleRepository) {
		return args -> {
			// Инициализация ролей
			if (roleRepository.count() == 0) {
				Role userRole = new Role();
				userRole.setId(1L);
				userRole.setName("USER");
				roleRepository.save(userRole);

				Role organizerRole = new Role();
				organizerRole.setId(2L);
				organizerRole.setName("ORGANIZER");
				roleRepository.save(organizerRole);

				Role adminRole = new Role();
				adminRole.setId(3L);
				adminRole.setName("ADMIN");
				roleRepository.save(adminRole);

				log.info("роли созданы");
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

//форматировать номер можно, передавать черз токен id password email