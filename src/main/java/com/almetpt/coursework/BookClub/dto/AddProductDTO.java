package com.almetpt.coursework.BookClub.dto;

import com.almetpt.coursework.BookClub.model.Category;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AddProductDTO {
    private String productName;
    private String productDescription;
    private BigDecimal price;
    private Category category;
    private Long bookId;
}
