package com.almetpt.coursework.bookclub.dto;
    
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
    
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long users;
    private long books;
    private long orders;
    private long events;
}
