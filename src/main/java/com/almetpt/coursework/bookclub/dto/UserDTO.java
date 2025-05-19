package com.almetpt.coursework.bookclub.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserDTO extends GenericDTO{
    private String email;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phone;
    private String address;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate birthDate;

    private RoleDTO role;
    private String changePasswordToken;
}
