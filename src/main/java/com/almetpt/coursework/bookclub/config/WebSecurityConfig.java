package com.almetpt.coursework.bookclub.config;

import com.almetpt.coursework.bookclub.config.jwt.JWTCookieFilter;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.Arrays;

import static com.almetpt.coursework.bookclub.constants.SecurityConstants.*;
import static com.almetpt.coursework.bookclub.constants.UserRoleConstants.*;

/**
 * Конфигурация безопасности приложения
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final JWTCookieFilter jwtCookieFilter;

    public WebSecurityConfig(BCryptPasswordEncoder bCryptPasswordEncoder,
                             CustomUserDetailsService customUserDetailsService,
                             JWTCookieFilter jwtCookieFilter) {
        this.jwtCookieFilter = jwtCookieFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/rest/auth/**").permitAll()
                        .requestMatchers(RESOURCE_WHITE_LIST.toArray(String[]::new)).permitAll()
                        .requestMatchers(BOOK_WHITE_LIST.toArray(String[]::new)).permitAll()
                        .requestMatchers(USER_WHITE_LIST.toArray(String[]::new)).permitAll()
                        .requestMatchers(EVENT_WHITE_LIST.toArray(String[]::new)).permitAll()
                        .requestMatchers(PRODUCT_WHITE_LIST.toArray(String[]::new)).permitAll()
                        .requestMatchers(BOOK_PERMISSIONS_LIST.toArray(String[]::new)).hasAnyRole(ADMIN, ORGANIZER)
                        .requestMatchers(USER_PERMISSIONS_LIST.toArray(String[]::new)).hasAnyRole(ADMIN, USER, ORGANIZER)
                        .requestMatchers(ORGANIZER_PERMISSIONS_LIST.toArray(String[]::new)).hasAnyRole(ADMIN, ORGANIZER)
                        .requestMatchers(ADMIN_PERMISSIONS_LIST.toArray(String[]::new)).hasRole(ADMIN)
                        .requestMatchers(AUTH_WHITE_LIST.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true); // Важно для работы с cookies
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
