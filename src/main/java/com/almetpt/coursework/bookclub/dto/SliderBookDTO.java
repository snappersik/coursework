package com.almetpt.coursework.bookclub.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliderBookDTO extends GenericDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String customDescription;
    private Integer position;
    private String backgroundImageUrl;
    private boolean hasLocalBackgroundImage;
    private String coverImageUrl;
}