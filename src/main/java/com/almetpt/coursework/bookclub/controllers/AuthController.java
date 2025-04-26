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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rest/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Контроллер для аутентификации и регистрации")
public class AuthController {

    @Value("${jwt.expiration}")
    private int jwtExpiration;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByEmail(loginDTO.getEmail());
            final String token = jwtTokenUtil.generateToken(userDetails);

            addJwtCookie(response, token);

            return ResponseEntity.ok("Authentication successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO registerRequest, HttpServletResponse response) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Пароли не совпадают");
        }

        UserDTO createdUser = userService.registerUser(registerRequest.getUserData(), registerRequest.getPassword());
        final UserDetails userDetails = userDetailsService.loadUserByEmail(createdUser.getEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);

        addJwtCookie(response, token);

        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Для разработки на localhost
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Logout successful");
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);  // Предотвращает доступ к cookie через JavaScript
        jwtCookie.setSecure(false);   // Для разработки на localhost без HTTPS
        jwtCookie.setPath("/");       // Доступно для всех путей
        jwtCookie.setMaxAge(jwtExpiration); // Время жизни куки из настроек
        jwtCookie.setAttribute("SameSite", "Lax"); // Для совместимости на localhost
        response.addCookie(jwtCookie);
    }
}