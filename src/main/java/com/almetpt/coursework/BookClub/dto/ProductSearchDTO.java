package com.almetpt.coursework.BookClub.dto;

import com.almetpt.coursework.BookClub.model.Category;
import com.almetpt.coursework.BookClub.model.Genre;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductSearchDTO {
    private String productName;
    private Category category;
}
