package com.almetpt.coursework.bookclub.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private LocalDate birthDate;
    private RoleDTO role;
    private String changePasswordToken;
}
