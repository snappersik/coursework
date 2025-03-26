package com.almetpt.coursework;

import com.almetpt.coursework.bookclub.model.Role;
import com.almetpt.coursework.bookclub.repository.RoleRepository;
import com.almetpt.coursework.bookclub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class CourseworkApplication {

	@Value("${server.port}")
	private String serverPort;

	public static void main(String[] args) {
		SpringApplication.run(CourseworkApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDatabase(RoleRepository roleRepository,
										  UserRepository userRepository,
										  BCryptPasswordEncoder bCryptPasswordEncoder) {
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
	public CommandLineRunner printApplicationUrl() {
		return args -> {
			log.info("Swagger UI: http://localhost:" + serverPort + "/swagger-ui/index.html");
			log.info("Application: http://localhost:" + serverPort + "/");
		};
	}
}
