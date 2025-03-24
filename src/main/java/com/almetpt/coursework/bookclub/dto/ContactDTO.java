package com.almetpt.coursework.bookclub.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ContactDTO {
    private String fromEmail;
    private String body;
    private String name;
    private String phone;
}
