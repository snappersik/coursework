package com.almetpt.coursework.bookclub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangePasswordDTO {
    @NotBlank(message = "Старый пароль не может быть пустым")
    private String oldPassword;
    
    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String newPassword;
}
