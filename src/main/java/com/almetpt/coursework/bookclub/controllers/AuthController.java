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

    private final AuthenticationManager authenticationManager;
    private final JWTTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // для HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600); // 1 час
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Authentication successful");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO registerRequest) {
        // registerRequest содержит все данные пользователя, включая пароль
        UserDTO createdUser = userService.registerUser(registerRequest.getUserData(), registerRequest.getPassword());
        return ResponseEntity.ok(createdUser);
    }


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
}
