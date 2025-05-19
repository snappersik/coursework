package com.almetpt.coursework.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private UserDTO userData;
    private String password;
    private String confirmPassword;
}
