package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.config.jwt.JWTTokenUtil;
import com.almetpt.coursework.bookclub.dto.LoginDTO;
import com.almetpt.coursework.bookclub.dto.RegisterDTO;
import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.service.UserService;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для аутентификации и регистрации пользователей
 */
@RestController
@RequestMapping("/api/rest/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Контроллер для аутентификации и регистрации")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    /**
     * Метод для входа пользователя
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        try {
            // Аутентификация пользователя
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );

            // Загрузка данных пользователя
            final UserDetails userDetails = userDetailsService.loadUserByEmail(loginDTO.getEmail());

            // Генерация JWT токена
            final String token = jwtTokenUtil.generateToken(userDetails);

            // Создание и настройка cookie с JWT токеном
            addJwtCookie(response, token);

            return ResponseEntity.ok("Authentication successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Метод для регистрации нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO registerRequest, HttpServletResponse response) {
        // Проверка совпадения паролей
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Пароли не совпадают");
        }

        // Регистрация пользователя
        UserDTO createdUser = userService.registerUser(registerRequest.getUserData(), registerRequest.getPassword());

        // Загрузка данных пользователя
        final UserDetails userDetails = userDetailsService.loadUserByEmail(createdUser.getEmail());

        // Генерация JWT токена
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Создание и настройка cookie с JWT токеном
        addJwtCookie(response, token);

        return ResponseEntity.ok(createdUser);
    }


    /**
     * Метод для выхода пользователя
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Удаление cookie при выходе
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Logout successful");
    }

    /**
     * Вспомогательный метод для добавления JWT токена в cookie
     */
    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);  // Предотвращает доступ к cookie через JavaScript
        jwtCookie.setSecure(true);    // Отправка cookie только по HTTPS
        jwtCookie.setPath("/");       // Доступно для всех путей
        jwtCookie.setMaxAge(3600);    // Время жизни cookie в секундах (1 час)
        response.addCookie(jwtCookie);
    }
}
