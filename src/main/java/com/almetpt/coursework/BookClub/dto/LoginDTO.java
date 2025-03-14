package com.almetpt.coursework.BookClub.dto;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

@ToString
@Getter
@Setter
public class LoginDTO {
    private String email;
    private String password;

}
