package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.config.jwt.JWTTokenUtil;
import com.almetpt.coursework.bookclub.dto.RegisterDTO;
import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.service.UserService;
import com.almetpt.coursework.bookclub.service.userdetails.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rest/users")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Пользователи", description = "Контроллер для работы с пользователями библиотеки")
public class UserController extends GenericController<User, UserDTO> {

    private final UserService userService;

    public UserController(UserService userService,
                          CustomUserDetailsService customUserDetailsService,
                          JWTTokenUtil jwtTokenUtil) {
        super(userService);
        this.userService = userService;
    }

    @Override
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody UserDTO dto) {
        throw new UnsupportedOperationException("Для создания пользователя используйте метод createUser");
    }

    @Operation(
            summary = "Создать нового пользователя",
            description = "Создает нового пользователя с указанными данными и ролью. Доступно только администраторам."
    )
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminAction
    public ResponseEntity<UserDTO> createUser(@RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(userService.createUser(registerDTO));
    }

    @Operation(
            summary = "Получить профиль текущего пользователя",
            description = "Возвращает данные профиля текущего аутентифицированного пользователя"
    )
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @Operation(
            summary = "Обновить профиль пользователя",
            description = "Обновляет данные профиля пользователя. Обычные пользователи могут обновлять только свой профиль."
    )
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateUserProfile(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateProfile(userDTO));
    }
}