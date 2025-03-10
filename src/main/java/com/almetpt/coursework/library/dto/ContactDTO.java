package com.almetpt.coursework.library.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class ContactDTO {
    private String fromEmail;
    private String body;
    private String name;
    private String phone;
}