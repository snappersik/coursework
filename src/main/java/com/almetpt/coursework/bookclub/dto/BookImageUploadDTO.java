package com.almetpt.coursework.bookclub.dto;

import lombok.Data;

@Data
public class BookImageUploadDTO {
    private Long bookId;
    private String imageUrl;
}