package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.config.jwt.JWTTokenUtil;
import com.almetpt.coursework.bookclub.dto.LoginDTO;
import com.almetpt.coursework.bookclub.dto.RegisterDTO;
import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.dto.ForgotPasswordRequestDTO; // New DTO
import com.almetpt.coursework.bookclub.dto.ResetPasswordRequestDTO; // New DTO
import com.almetpt.coursework.bookclub.service.UserService;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation; // For Swagger
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid; // For DTO validation
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Added for logging

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j // Added for logging
@RestController
@RequestMapping("/api/rest/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Контроллер для аутентификации, регистрации и восстановления пароля")
public class AuthController {

    @Value("${jwt.expiration}")
    private int jwtExpiration; // Assuming this is in seconds
    private final AuthenticationManager authenticationManager;
    private final JWTTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя в систему", description = "Аутентифицирует пользователя и возвращает JWT в cookie.")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

            final UserDetails userDetails = userDetailsService.loadUserByEmail(loginDTO.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);

            addJwtCookie(response, token);

            // Optionally return user info or just success
            UserDTO userInfo = userService.getUserByEmail(loginDTO.getEmail());
            return ResponseEntity.ok(userInfo); // Return user info on successful login
        } catch (Exception e) {
            log.error("Authentication failed for email {}: {}", loginDTO.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", description = "Регистрирует пользователя и возвращает JWT в cookie.")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDTO registerRequest,
            HttpServletResponse response) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Пароли не совпадают");
        }
        try {
            UserDTO createdUser = userService.registerUser(registerRequest.getUserData(),
                    registerRequest.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByEmail(createdUser.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);

            addJwtCookie(response, token);

            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("Registration failed for email {}: {}", registerRequest.getUserData().getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + e.getMessage());
        }
    }

@PostMapping("/logout")
@Operation(summary = "Выход пользователя из системы")
public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    // Удаление куки
    Cookie jwtCookie = new Cookie("jwt", null);
    jwtCookie.setHttpOnly(true);
    jwtCookie.setSecure(request.isSecure());
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(0);
    jwtCookie.setAttribute("SameSite", "Lax");
    response.addCookie(jwtCookie);
    
    // Добавление заголовка Clear-Site-Data
    response.setHeader("Clear-Site-Data", "\"cookies\", \"storage\"");
    
    log.info("Сессия пользователя завершена");
    return ResponseEntity.ok().build();
}


    @PostMapping("/forgot-password")
    @Operation(summary = "Запрос на сброс пароля", description = "Инициирует процесс сброса пароля. Отправляет письмо с инструкциями, если email существует в системе.")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO) {
        try {
            userService.initiatePasswordReset(requestDTO.getEmail());
            // Always return a generic success message to prevent email enumeration
            return ResponseEntity.ok(
                    "Если указанный email зарегистрирован, на него будет отправлено письмо с инструкциями по сбросу пароля.");
        } catch (Exception e) {
            log.error("Ошибка при запросе сброса пароля для email {}: {}", requestDTO.getEmail(), e.getMessage(), e);
            // Even in case of an internal server error, return a generic message for
            // security
            return ResponseEntity.ok(
                    "Если указанный email зарегистрирован, на него будет отправлено письмо с инструкциями по сбросу пароля.");
        }
    }

    @GetMapping("/validate-reset-token")
    @Operation(summary = "Проверка валидности токена сброса пароля", description = "Проверяет, действителен ли предоставленный токен для сброса пароля.")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean isValid = userService.isPasswordResetTokenValid(token);
        if (isValid) {
            return ResponseEntity.ok("Токен действителен.");
        } else {
            // Avoid giving too much detail about why it's invalid (e.g., expired vs. not
            // found)
            return ResponseEntity.badRequest().body("Недействительный или истекший токен.");
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Сброс пароля", description = "Устанавливает новый пароль для пользователя на основе валидного токена сброса.")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO requestDTO) {
        if (!requestDTO.getNewPassword().equals(requestDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Пароли не совпадают.");
        }
        try {
            userService.finalizePasswordReset(requestDTO.getToken(), requestDTO.getNewPassword());
            return ResponseEntity.ok("Пароль успешно изменен.");
        } catch (UserService.InvalidTokenException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserService.TokenExpiredException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при сбросе пароля с токеном {}: {}", requestDTO.getToken(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при сбросе пароля.");
        }
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // false for localhost development, true for production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtExpiration); // jwtExpiration is in seconds
        jwtCookie.setAttribute("SameSite", "Lax"); // Or "Strict" or "None" (if Secure is true)
        response.addCookie(jwtCookie);
    }
}
