package com.almetpt.coursework.BookClub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO extends GenericDTO {
    private Long id;
    private String productName;
    private String productDescription;
    private BigDecimal price;
    private String category;
}
