package com.almetpt.coursework.bookclub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Токен не может быть пустым")
    private String token;

    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String newPassword;

    @NotBlank(message = "Подтверждение пароля не может быть пустым")
    private String confirmPassword;
}
