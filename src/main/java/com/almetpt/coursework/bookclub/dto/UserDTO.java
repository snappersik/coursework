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
    private String id;
    private String email;
    private String password;
    private LocalDate birthDate;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phone;
    private String address;
    private RoleDTO role;
    private String changePasswordToken;
    private boolean isDeleted;
}
