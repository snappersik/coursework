package com.almetpt.coursework.bookclub.config;

import com.almetpt.coursework.bookclub.config.jwt.JWTCookieFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import static com.almetpt.coursework.bookclub.constants.SecurityConstants.*;
import static com.almetpt.coursework.bookclub.constants.UserRoleConstants.ADMIN;
import static com.almetpt.coursework.bookclub.constants.UserRoleConstants.ORGANIZER;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JWTCookieFilter jwtCookieFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(RESOURCES_WHITE_LIST.toArray(String[]::new)).permitAll()
                .requestMatchers(AUTH_WHITE_LIST.toArray(String[]::new)).permitAll()
                .requestMatchers(PUBLIC_GET_LIST.toArray(String[]::new)).permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/rest/users/profile").authenticated()
                // Явное разрешение для путей админ-панели
                .requestMatchers(ADMIN_PERMISSIONS_LIST.toArray(String[]::new)).hasRole(ADMIN)
                .requestMatchers(ORGANIZER_PERMISSIONS_LIST.toArray(String[]::new)).hasRole(ORGANIZER)
                .requestMatchers(AUTHENTICATED_PERMISSIONS.toArray(String[]::new)).authenticated()
                // Добавляем разрешение для GET-запросов к путям админ-панели
                .requestMatchers(HttpMethod.GET, "/api/rest/admin/**").hasRole(ADMIN)
                // Явно разрешаем POST-запросы на создание заказа для аутентифицированных пользователей
                .requestMatchers(HttpMethod.POST, "/api/rest/orders/create").authenticated()
                .anyRequest().denyAll())
            .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        config.setExposedHeaders(Arrays.asList("X-Auth-Token"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
