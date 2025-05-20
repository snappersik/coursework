package com.almetpt.coursework.bookclub.controllers;

import com.almetpt.coursework.bookclub.annotations.AdminAction;
import com.almetpt.coursework.bookclub.dto.ChangePasswordDTO;
import com.almetpt.coursework.bookclub.dto.RegisterDTO;
import com.almetpt.coursework.bookclub.dto.UserDTO;
import com.almetpt.coursework.bookclub.model.User;
import com.almetpt.coursework.bookclub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/rest/users")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "Пользователи", description = "Контроллер для работы с пользователями библиотеки")
public class UserController extends GenericController<User, UserDTO> {

    private final UserService userService;

    public UserController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @Operation(summary = "Получить страницу активных пользователей", description = "Возвращает страницу активных пользователей с пагинацией")
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getActiveUsersPaginated(
            @PageableDefault(size = 20, sort = "email") Pageable pageable) {
        Page<UserDTO> users = userService.listAllNotDeleted(pageable);
        return ResponseEntity.ok(users);
    }

    @Override
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody UserDTO dto) {
        throw new UnsupportedOperationException("Для создания пользователя используйте метод createUser");
    }

    @Operation(summary = "Создать нового пользователя", description = "Создает нового пользователя с указанными данными и ролью. Доступно только администраторам.")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @AdminAction
    public ResponseEntity<UserDTO> createUser(@RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(userService.createUser(registerDTO));
    }

    @Operation(summary = "Получить профиль текущего пользователя", description = "Возвращает данные профиля текущего аутентифицированного пользователя")
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @Operation(summary = "Обновить профиль пользователя", description = "Обновляет данные профиля пользователя. Обычные пользователи могут обновлять только свой профиль.")
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateUserProfile(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateProfile(userDTO));
    }

    @PostMapping("/profile/avatar-upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserDTO updatedUser = userService.uploadUserAvatar(file);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Ошибка при загрузке аватара: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при загрузке аватара: " + e.getMessage()));
        }
    }

    @PutMapping("/profile/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserPassword(@RequestBody ChangePasswordDTO changePasswordDTO,
            Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        try {
            userService.changePasswordForAuthenticatedUser(email, changePasswordDTO.getOldPassword(),
                    changePasswordDTO.getNewPassword());
            return ResponseEntity.ok().body(Map.of("message", "Пароль успешно изменен"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Неверный старый пароль"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Ошибка при смене пароля для пользователя {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при смене пароля"));
        }
    }

}