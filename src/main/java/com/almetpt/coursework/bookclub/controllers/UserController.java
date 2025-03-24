package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.config.jwt.JWTTokenUtil;
import com.almetpt.coursework.bookclub.dto.LoginDTO;
import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.service.GenericService;
import com.almetpt.coursework.bookclub.service.UserService;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rest/users")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Пользователи", description = "Контроллер для работы с пользователями библиотеки")
public class UserController extends GenericController<User, UserDTO> {

    private final CustomUserDetailsService customUserDetailsService;
    private final JWTTokenUtil jwtTokenUtil;
    private final UserService userService;

    public UserController(GenericService<User, UserDTO> genericService,
                          CustomUserDetailsService customUserDetailsService,
                          JWTTokenUtil jwtTokenUtil,
                          UserService userService) {
        super(genericService);
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        Map<String, Object> responseMap = new HashMap<>();
        // Изменение: используем email вместо login
        UserDetails foundUser = customUserDetailsService.loadUserByUsername(loginDTO.getEmail());
        log.info("foundUser: {}", foundUser);

        if (foundUser == null || !userService.checkPassword(loginDTO.getPassword(), foundUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ошибка авторизации! \n Неверный пароль...");
        }

        String token = jwtTokenUtil.generateToken(foundUser);

        // Установка cookie с JWT токеном
        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true); // Защита от доступа через JavaScript
        cookie.setMaxAge(86400); // Устанавливаем время жизни cookie (1 день)
        response.addCookie(cookie);

        // Изменение: используем email вместо username
        responseMap.put("email", foundUser.getUsername());
        responseMap.put("role", foundUser.getAuthorities());

        return ResponseEntity.ok().body(responseMap);
    }
}
