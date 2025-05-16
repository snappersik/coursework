package com.almetpt.coursework.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookImageDTO {
    private Long bookId;
    private String coverImageUrl;
    private String coverImageFilename;
    private boolean hasLocalImage;
    private String originalCoverImageFilename;
}