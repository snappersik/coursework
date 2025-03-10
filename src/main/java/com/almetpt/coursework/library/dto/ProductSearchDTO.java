package com.almetpt.coursework.library.dto;

import com.almetpt.coursework.library.model.Category;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductSearchDTO {
    private String bookTitle;
    private String authorName;
    private Category genre;
}
